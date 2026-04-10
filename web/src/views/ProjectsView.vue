<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>{{ role === 'admin' ? '项目管理' : '项目查询' }}</h2>
        <p class="muted">支持项目列表检索、编辑和详情查看。</p>
      </div>
      <el-button v-if="role === 'admin'" type="primary" @click="openCreate">新增项目</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="项目名称">
          <el-input v-model="filters.projectName" placeholder="输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="filters.principalName" placeholder="输入负责人" clearable />
        </el-form-item>
      </el-form>

      <el-table :data="filteredProjects" empty-text="暂无数据">
        <el-table-column prop="projectName" label="项目名称" min-width="220" />
        <el-table-column prop="projectCode" label="项目编号" width="160" />
        <el-table-column prop="principalName" label="负责人" width="120" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="总预算" width="150">
          <template #default="{ row }">{{ formatCurrency(row.totalBudget) }}</template>
        </el-table-column>
        <!-- ★ 新增 -->
        <el-table-column label="已使用" width="150">
          <template #default="{ row }">{{ formatCurrency(row.usedAmount) }}</template>
        </el-table-column>
        <!-- ★ 新增 -->
        <el-table-column label="剩余金额" width="150">
          <template #default="{ row }">{{ formatCurrency(row.remainingAmount) }}</template>
        </el-table-column>
        <!-- ★ 新增 -->
        <el-table-column label="执行率" min-width="220">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.executionRate || 0)" :stroke-width="14" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="performance" label="绩效等级" width="120">
          <template #default="{ row }">
            <el-tag :type="performanceType(row.performance)">{{ row.performance }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/projects/${row.id}`)">详情</el-button>
            <el-button v-if="role === 'admin'" link type="warning" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm v-if="role === 'admin'" title="确认删除该项目？" @confirm="remove(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="editing.id ? '编辑项目' : '新增项目'" width="560px">
      <el-form :model="editing" label-width="96px">
        <el-form-item label="项目名称"><el-input v-model="editing.projectName" /></el-form-item>
        <el-form-item label="项目编号"><el-input v-model="editing.projectCode" /></el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="editing.principalId">
            <el-option v-for="user in researchers" :key="user.id" :label="user.name" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="editing.startDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="editing.endDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="总预算"><el-input-number v-model="editing.totalBudget" :min="0" :step="1000" /></el-form-item>
        <el-form-item label="绩效等级">
          <el-select v-model="editing.performance">
            <el-option label="优" value="优" />
            <el-option label="良" value="良" />
            <el-option label="中" value="中" />
            <el-option label="差" value="差" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { projectApi } from '@/api/projectApi'
import { userApi } from '@/api/userApi'
import { getRole, getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'

const role = getRole()
const currentUser = getUser()
const projects = ref([])
const researchers = ref([])
const visible = ref(false)
const filters = reactive({ projectName: '', principalName: '' })
const editing = reactive({
  id: null,
  projectName: '',
  projectCode: '',
  principalId: null,
  startDate: '',
  endDate: '',
  totalBudget: 0,
  performance: '中'
})

const filteredProjects = computed(() =>
  projects.value.filter((item) => {
    const matchesName = !filters.projectName || item.projectName.includes(filters.projectName)
    const matchesLeader = !filters.principalName || item.principalName.includes(filters.principalName)
    return matchesName && matchesLeader
  })
)

function performanceType(value) {
  return { 优: 'success', 良: 'primary', 中: 'warning', 差: 'danger' }[value] || 'info'
}

function resetEditing() {
  Object.assign(editing, {
    id: null,
    projectName: '',
    projectCode: '',
    principalId: researchers.value[0]?.id || null,
    startDate: '',
    endDate: '',
    totalBudget: 0,
    performance: '中'
  })
}

async function loadData() {
  const [projectResponse, userResponse] = await Promise.all([projectApi.getList(), userApi.getList()])
  projects.value = role === 'researcher'
    ? projectResponse.data.filter((item) => item.principalId === currentUser?.id)
    : projectResponse.data
  researchers.value = userResponse.data.filter((item) => item.role === 'researcher')
}

function openCreate() {
  resetEditing()
  visible.value = true
}

function openEdit(row) {
  Object.assign(editing, row)
  visible.value = true
}

async function submit() {
  if (editing.id) {
    await projectApi.update(editing)
    ElMessage.success('项目已更新')
  } else {
    await projectApi.add(editing)
    ElMessage.success('项目已新增')
  }
  visible.value = false
  await loadData()
}

async function remove(id) {
  await projectApi.delete(id)
  ElMessage.success('项目已删除')
  await loadData()
}

onMounted(async () => {
  await loadData()
  if (!editing.principalId) resetEditing()
})
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.muted {
  color: #64748b;
}
</style>
