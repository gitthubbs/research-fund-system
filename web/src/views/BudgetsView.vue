<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>{{ role === 'admin' ? '预算管理' : '预算/支出查询' }}</h2>
        <p class="muted">展示预算执行率，并可按项目分类设置预算。</p>
      </div>
      <div class="toolbar-actions">
        <!-- ★ 新增 -->
        <el-button v-if="role === 'admin'" type="primary" @click="visible = true">新增预算</el-button>
        <el-button v-if="role === 'admin'" @click="$router.push('/warning-settings')">预警配置</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="项目">
          <el-select v-model="selectedProjectId" style="width: 220px" @change="loadBudgets">
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="budgets" empty-text="暂无数据">
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="categoryName" label="预算类别" width="140" />
        <el-table-column label="预算金额" width="140">
          <template #default="{ row }">{{ formatCurrency(row.budgetAmount) }}</template>
        </el-table-column>
        <el-table-column label="已执行" width="140">
          <template #default="{ row }">{{ formatCurrency(row.spentAmount) }}</template>
        </el-table-column>
        <el-table-column label="执行率" min-width="220">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                :percentage="safeExecutionRate(row.executionRate)"
                :color="progressColor(Number(row.executionRate || 0))"
              />
              <el-tag v-if="Number(row.executionRate || 0) >= threshold" type="danger">
                超支预警
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-tag v-if="Number(row.executionRate || 0) >= threshold" type="danger" effect="dark">
              超支预警
            </el-tag>
            <span v-else class="muted">正常</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ★ 新增 -->
    <el-dialog v-model="visible" title="新增预算" width="520px">
      <el-form :model="editing" label-width="90px">
        <el-form-item label="项目">
          <el-select v-model="editing.projectId" style="width: 100%">
            <el-option v-for="project in projects" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editing.categoryId" style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额">
          <el-input-number v-model="editing.budgetAmount" :min="0" :precision="2" :step="1000" style="width: 100%" />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { budgetApi } from '@/api/budgetApi'
import { projectApi } from '@/api/projectApi'
import { settingsApi } from '@/api/settingsApi'
import { categoryApi } from '@/api/categoryApi'
import { getRole, getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'

const role = getRole()
const currentUser = getUser()
const projects = ref([])
const selectedProjectId = ref()
const budgets = ref([])
const threshold = ref(95)
// ★ 新增
const categories = ref([])
// ★ 新增
const visible = ref(false)
// ★ 新增
const editing = reactive({
  projectId: undefined,
  categoryId: undefined,
  budgetAmount: 0
})

function progressColor(rate) {
  if (rate < 80) return '#67C23A'
  if (rate <= 95) return '#E6A23C'
  return '#F56C6C'
}

function safeExecutionRate(rate) {
  return Number(Number(rate || 0).toFixed(1))
}

async function loadProjects() {
  const response = await projectApi.getList()
  projects.value = role === 'researcher'
    ? response.data.filter((item) => item.principalId === currentUser?.id)
    : response.data
  selectedProjectId.value = projects.value[0]?.id
  if (!editing.projectId && projects.value.length) {
    editing.projectId = projects.value[0].id
  }
}

// ★ 新增
async function loadCategories() {
  const response = await categoryApi.getList()
  categories.value = response.data || []
  if (!editing.categoryId && categories.value.length) {
    editing.categoryId = categories.value[0].id
  }
}

async function loadBudgets() {
  if (!selectedProjectId.value) {
    budgets.value = []
    return
  }
  const response = await budgetApi.getByProject(selectedProjectId.value)
  budgets.value = Array.isArray(response.data) ? response.data : []
}

// ★ 新增
async function submit() {
  await budgetApi.add(editing)
  ElMessage.success('预算已新增')
  visible.value = false
  selectedProjectId.value = editing.projectId
  await loadBudgets()
}

onMounted(async () => {
  const settingsResponse = await settingsApi.getWarningThreshold()
  threshold.value = Number(settingsResponse.data?.value || 95)
  await Promise.all([loadProjects(), loadCategories()])
  await loadBudgets()
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

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.muted {
  color: #64748b;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
