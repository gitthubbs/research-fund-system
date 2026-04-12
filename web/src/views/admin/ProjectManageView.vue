<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>项目管理</h2>
        <p class="muted">全系统科研项目查阅及基础信息维护。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增项目</el-button>
    </div>

    <el-card shadow="never">
      <div class="filters">
        <el-input v-model="filters.projectName" placeholder="搜索项目名称" style="width: 200px" />
        <el-input v-model="filters.principalName" placeholder="搜索负责人" style="width: 200px" />
      </div>

      <el-table :data="filteredProjects">
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectCode" label="项目编号" width="160" />
        <el-table-column prop="principalName" label="负责人" width="120" />
        <el-table-column label="执行进度" width="180">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.executionRate || 0)" :stroke-width="14" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="绩效等级" width="120">
          <template #default="{ row }">
            <PerformanceTag :performance="row.performance" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/projects/${row.id}`)">详情</el-button>
            <el-button link type="warning" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该项目？" @confirm="remove(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 管理员专用表单弹窗 -->
    <el-dialog v-model="visible" :title="editing.id ? '编辑项目' : '新增项目'" width="640px" destroy-on-close>
      <ProjectForm 
        ref="formRef"
        v-model="editing" 
        :is-admin="true" 
        :researchers="researchers"
        label-width="100px"
        label-position="right"
      />
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { projectApi } from '@/api/projectApi'
import { userApi } from '@/api/userApi'
import StatusTag from '@/components/project/StatusTag.vue'
import PerformanceTag from '@/components/project/PerformanceTag.vue'
import ProjectForm from '@/components/project/ProjectForm.vue'

const projects = ref([])
const researchers = ref([])
const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const filters = reactive({
  projectName: '',
  principalName: ''
})

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

async function loadData() {
  const [projectRes, userRes] = await Promise.all([projectApi.getList(), userApi.getList()])
  projects.value = projectRes.data
  researchers.value = userRes.data.filter(u => u.role === 'researcher')
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

function openCreate() {
  resetEditing()
  visible.value = true
}

function openEdit(row) {
  Object.assign(editing, row)
  visible.value = true
}

async function submit() {
  const valid = await formRef.value.validate()
  if (!valid) return

  const payload = {
    id: editing.id,
    projectName: editing.projectName,
    projectCode: editing.projectCode,
    principalId: editing.principalId,
    startDate: editing.startDate,
    endDate: editing.endDate,
    totalBudget: editing.totalBudget,
    performance: editing.performance
  }

  submitting.value = true
  try {
    if (editing.id) {
      await projectApi.update(payload)
      ElMessage.success('更新成功')
    } else {
      await projectApi.add(payload)
      ElMessage.success('新增成功')
    }
    visible.value = false
    await loadData()
  } catch (err) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(id) {
  await projectApi.delete(id)
  ElMessage.success('项目已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.filters { display: flex; gap: 12px; margin-bottom: 20px; }
.muted { color: #64748b; font-size: 14px; margin: 4px 0 0; }
</style>
