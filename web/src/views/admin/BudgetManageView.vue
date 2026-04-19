<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>预算调剂与管理</h2>
        <p class="muted">展示各项目的预算执行率，并支持跨科目的经费总额调剂。</p>
      </div>
      <div class="toolbar-actions">
        <el-button type="primary" @click="openAdd" :disabled="!selectedProjectId">新增预算项</el-button>
        <el-button @click="$router.push('/warning-settings')">预警配置</el-button>
      </div>
    </div>

    <!-- 预算概览统计 -->
    <div v-if="selectedProject" class="summary-section">
      <el-descriptions border :column="3" direction="vertical">
        <el-descriptions-item label="项目总预算">
          <span class="amount-text">{{ formatCurrency(selectedProject.totalBudget) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="已分配金额">
          <span class="amount-text" :class="{ 'text-success': isBalanced }">
            {{ formatCurrency(allocatedTotal) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="待平衡差额">
          <span class="amount-text" :class="{ 'text-danger': !isBalanced }">
            {{ formatCurrency(remainingAmount) }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="筛选项目">
          <el-select v-model="selectedProjectId" style="width: 400px" @change="loadBudgets" filterable placeholder="请选择或搜索项目">
            <el-option-group v-for="group in groupedProjects" :key="group.label" :label="group.label">
              <el-option
                v-for="project in group.options"
                :key="project.id"
                :label="`[${project.projectCode}] ${project.projectName}`"
                :value="project.id"
              >
                <div class="option-content">
                  <span class="proj-name">{{ project.projectName }}</span>
                  <span class="proj-code">{{ project.projectCode }}</span>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="budgets" empty-text="选择项目以查看预算分布">
        <el-table-column prop="categoryName" label="预算类别" width="160" />
        <el-table-column label="预算金额" width="150">
          <template #default="{ row }"><strong>{{ formatCurrency(row.budgetAmount) }}</strong></template>
        </el-table-column>
        <el-table-column label="已执行" width="140">
          <template #default="{ row }">{{ formatCurrency(row.spentAmount) }}</template>
        </el-table-column>
        <el-table-column label="执行进度" min-width="200">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                :percentage="safeExecutionRate(row.executionRate)"
                :color="progressColor(Number(row.executionRate || 0))"
                style="flex: 1"
              />
              <el-tag v-if="Number(row.executionRate || 0) >= threshold" type="danger" effect="dark" size="small">
                超支预警
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">调剂</el-button>
            <el-popconfirm title="确定删除该预算条目吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="isEdit ? '预算调剂' : '新增预算条目'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="editing" :rules="rules" label-width="90px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="editing.projectId" style="width: 100%" filterable disabled>
            <el-option v-for="project in projects" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算分类" prop="categoryId">
          <el-select v-model="editing.categoryId" style="width: 100%" :disabled="isEdit" placeholder="请选择科目">
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.categoryName" 
              :value="category.id"
              :disabled="budgets.some(b => b.categoryId === category.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额" prop="budgetAmount">
          <el-input-number v-model="editing.budgetAmount" :min="0.01" :precision="2" :step="1000" style="width: 100%" />
          <div class="form-help">调剂规则：如增加 A 科目预算，需同步减少 B 模块预算，确保总额不变。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">保存更改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { budgetApi } from '@/api/budgetApi'
import { projectApi } from '@/api/projectApi'
import { settingsApi } from '@/api/settingsApi'
import { categoryApi } from '@/api/categoryApi'
import { formatCurrency } from '@/utils/format'

const projects = ref([])
const selectedProjectId = ref()
const budgets = ref([])
const threshold = ref(95)
const categories = ref([])
const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const rules = {
  projectId: [{ required: true, message: '项目不能为空', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择预算科目', trigger: 'change' }],
  budgetAmount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ]
}

const editing = reactive({
  id: null,
  projectId: undefined,
  categoryId: undefined,
  budgetAmount: 0
})

const selectedProject = computed(() => {
  return projects.value.find(p => p.id === selectedProjectId.value)
})

const allocatedTotal = computed(() => {
  return budgets.value.reduce((acc, b) => acc + (b.budgetAmount || 0), 0)
})

const remainingAmount = computed(() => {
  return (selectedProject.value?.totalBudget || 0) - allocatedTotal.value
})

const isBalanced = computed(() => {
  return Math.abs(remainingAmount.value) < 0.01
})

const groupedProjects = computed(() => {
  // 1. 强制过滤：只保留“执行中”的项目
  const executingOnes = projects.value.filter(p => p.status === '执行中')
  
  // 2. 按负责人分组
  const groups = {}
  executingOnes.forEach(p => {
    const name = p.principalName || '未知负责人'
    if (!groups[name]) {
      groups[name] = {
        label: `${name} 负责的项目`,
        options: []
      }
    }
    groups[name].options.push(p)
  })

  return Object.values(groups).sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'))
})

function progressColor(rate) {
  if (rate < 80) return '#67C23A'
  if (rate <= 95) return '#E6A23C'
  return '#F56C6C'
}

function safeExecutionRate(rate) {
  return Math.min(100, Number(Number(rate || 0).toFixed(1)))
}

async function loadData() {
  const [projectRes, categoryRes, settingsRes] = await Promise.all([
    projectApi.getList(),
    categoryApi.getList(),
    settingsApi.getWarningThreshold()
  ])
  projects.value = projectRes.data
  categories.value = categoryRes.data || []
  threshold.value = Number(settingsRes.data?.value || 95)
  
  if (groupedProjects.value.length > 0) {
    selectedProjectId.value = groupedProjects.value[0].options[0].id
    await loadBudgets()
  }
}

async function loadBudgets() {
  if (!selectedProjectId.value) return
  const response = await budgetApi.getByProject(selectedProjectId.value)
  budgets.value = response.data || []
}

function openAdd() {
  isEdit.value = false
  editing.id = null
  editing.projectId = selectedProjectId.value
  editing.categoryId = undefined
  editing.budgetAmount = 0
  visible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editing.id = row.id
  editing.projectId = row.projectId
  editing.categoryId = row.categoryId
  editing.budgetAmount = row.budgetAmount
  visible.value = true
}

async function handleDelete(id) {
  try {
    await budgetApi.delete(id)
    ElMessage.success('条目已删除')
    await loadBudgets()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

async function submit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await budgetApi.update(editing)
        ElMessage.success('预算已调剂')
      } else {
        await budgetApi.add(editing)
        ElMessage.success('预算项已添加')
      }
      visible.value = false
      await loadBudgets()
    } catch (err) {
      console.error('提交失败:', err)
      // 拦截器已处理错误提示，此处仅打印日志
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.toolbar-actions { display: flex; gap: 8px; }
.muted { color: #64748b; font-size: 14px; }
.progress-cell { display: flex; align-items: center; gap: 12px; }
.summary-section { margin-bottom: 8px; }
.amount-text { font-size: 18px; font-weight: 700; font-family: 'Courier New', Courier, monospace; }
.text-success { color: #10b981; }
.text-danger { color: #ef4444; }
.option-content { display: flex; justify-content: space-between; align-items: center; width: 100%; gap: 12px; }
.proj-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 250px; }
.proj-code { color: #94a3b8; font-size: 12px; font-family: monospace; }
.form-help { font-size: 12px; color: #64748b; margin-top: 8px; font-style: italic; }
</style>
