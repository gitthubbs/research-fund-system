<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>预算编制</h2>
        <p class="muted">规划项目经费构成，确保分项预算之和等于项目总额。</p>
      </div>
    </div>

    <el-card shadow="never">
      <div class="project-selector">
        <el-form :inline="true">
          <el-form-item label="选择项目">
            <el-select v-model="selectedProjectId" style="width: 320px" @change="loadBudgets">
              <el-option
                v-for="project in projects"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        
        <el-descriptions v-if="selectedProject" :column="2" border class="project-meta">
          <el-descriptions-item label="项目编号">
            <template #label><el-icon><Document /></el-icon> 项目编号</template>
            <span class="meta-value">{{ selectedProject.projectCode }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">
            <template #label><el-icon><User /></el-icon> 负责人</template>
            <span class="meta-value">{{ selectedProject.principalName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="周期开始">
            <template #label><el-icon><Calendar /></el-icon> 开始日期</template>
            <span class="meta-value">{{ selectedProject.startDate }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="周期结束">
            <template #label><el-icon><Calendar /></el-icon> 结束日期</template>
            <span class="meta-value">{{ selectedProject.endDate }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 预算概览与饼图可视化 -->
    <el-row :gutter="16" class="summary-section" type="flex">
      <el-col :xs="24" :sm="14" :lg="16" class="metrics-container">
        <el-row :gutter="16" class="metrics-row">
          <el-col :span="12">
            <el-card shadow="never" class="summary-card total">
              <template #header>项目立项总额</template>
              <div class="amount">{{ formatCurrency(selectedProject?.totalBudget || 0) }}</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="summary-card allocated">
              <template #header>当前已分配金额</template>
              <div class="amount" :class="{ 'text-success': allocatedTotal === selectedProject?.totalBudget }">
                {{ formatCurrency(allocatedTotal) }}
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="16" class="metrics-row" style="margin-top: 16px">
          <el-col :span="12">
            <el-card shadow="never" class="summary-card remaining">
              <template #header>待分配余额</template>
              <div class="amount" :class="{ 'text-danger': remainingAmount < 0 }">
                {{ formatCurrency(remainingAmount) }}
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
             <div class="alert-box" v-if="canEditBudget" style="height: 100%; width: 100%">
              <el-alert 
                v-if="remainingAmount !== 0"
                type="warning" 
                description="分项之和须等于立项总额"
                show-icon 
                :closable="false" 
                style="height: 100%"
              >
                <template #title>
                  <span class="status-title warning">分配尚未平衡</span>
                </template>
              </el-alert>
              <el-alert 
                v-else
                type="success" 
                description="现在可以确认并启动项目"
                show-icon 
                :closable="false"
                style="height: 100%"
              >
                <template #title>
                  <span class="status-title success">分配已达成平衡</span>
                </template>
              </el-alert>
            </div>
          </el-col>
        </el-row>
      </el-col>
      
      <el-col :xs="24" :sm="10" :lg="8">
        <el-card shadow="never" class="chart-card">
          <div ref="chartRef" class="allocation-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="toolbar-actions">
        <el-button 
          v-if="canEditBudget" 
          type="primary" 
          plain
          :icon="Plus"
          @click="openAdd"
          :disabled="remainingAmount <= 0"
        >新增预算项</el-button>
        <el-button 
          v-if="canEditBudget" 
          type="success" 
          :icon="Check"
          @click="handleFinalConfirm"
          :disabled="remainingAmount !== 0"
          :loading="activating"
        >确认预算并启动</el-button>
      </div>

    <el-card shadow="never">

      <el-table :data="budgets" empty-text="当前项目暂无预算数据，请新增预算项" border stripe>
        <el-table-column prop="categoryName" label="预算类别" width="180" />
        <el-table-column label="预算金额" width="160" align="right" header-align="right">
          <template #default="{ row }">
            <span class="currency-font">{{ formatCurrency(row.budgetAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!canEditBudget" label="已支出" width="160" align="right" header-align="right">
          <template #default="{ row }">
            <span class="currency-font">{{ formatCurrency(row.spentAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="执行/分配进度" min-width="260">
          <template #default="{ row }">
            <div class="progress-details">
              <div class="progress-info">
                <span class="progress-label">
                  {{ !canEditBudget ? '已执行' : '已分配' }}: 
                  <strong>{{ !canEditBudget ? safeExecutionRate(row.executionRate) : safeAllocationRate(row.budgetAmount) }}%</strong>
                </span>
                <el-tag v-if="!canEditBudget && Number(row.executionRate || 0) >= threshold" type="danger" size="small" effect="dark">
                  超支预警
                </el-tag>
              </div>
              <el-progress
                v-if="!canEditBudget"
                :percentage="safeExecutionRate(row.executionRate)"
                :color="progressColor(Number(row.executionRate || 0))"
                :show-text="false"
                :stroke-width="10"
              />
              <el-progress
                v-else
                :percentage="safeAllocationRate(row.budgetAmount)"
                :show-text="false"
                :stroke-width="10"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="canEditBudget" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="info-footer">
      <el-alert title="预算编制规范" type="info" show-icon :closable="false">
        <template #default>
          <ul class="specs">
            <li>项目预算确定后，原则上不予变更。</li>
            <li>在“待编制”状态下，您可以自由增减科目，但最终提交时总额必须闭环。</li>
            <li>如需进行跨科目调剂，请线下申请并联系财务管理员。</li>
          </ul>
        </template>
      </el-alert>
    </div>

    <el-dialog v-model="visible" :title="isEdit ? '修改预算项' : '新增预算编制项'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="editing" :rules="rules" label-width="90px">
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
          <el-input-number 
            v-model="editing.budgetAmount" 
            :min="0.01" 
            :precision="2" 
            :step="100" 
            style="width: 100%" 
            placeholder="请输入金额"
          />
          <div class="form-tip">当前该项最大可用额度：{{ formatCurrency(availableForEdit) }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { budgetApi } from '@/api/budgetApi'
import { projectApi } from '@/api/projectApi'
import { settingsApi } from '@/api/settingsApi'
import { categoryApi } from '@/api/categoryApi'
import { getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'
import { Plus, Check, Document, User, Calendar } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const projects = ref([])
const selectedProjectId = ref()
const budgets = ref([])
const threshold = ref(95)
const categories = ref([])
const visible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const activating = ref(false)
const route = useRoute()
const router = useRouter()
const chartRef = ref(null)
const formRef = ref(null)
let myChart = null

const currentUser = getUser() || {}

const editing = reactive({
  id: null,
  projectId: undefined,
  categoryId: undefined,
  budgetAmount: 0
})

const rules = {
  categoryId: [{ required: true, message: '请选择预算科目', trigger: 'change' }],
  budgetAmount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ]
}

const selectedProject = computed(() => {
  return projects.value.find(item => item.id === selectedProjectId.value)
})

const allocatedTotal = computed(() => {
  return budgets.value.reduce((acc, b) => acc + (b.budgetAmount || 0), 0)
})

const remainingAmount = computed(() => {
  const total = selectedProject.value?.totalBudget || 0
  return total - allocatedTotal.value
})

const availableForEdit = computed(() => {
  const total = selectedProject.value?.totalBudget || 0
  const otherAllocated = budgets.value
    .filter(b => b.id !== editing.id)
    .reduce((acc, b) => acc + (b.budgetAmount || 0), 0)
  return total - otherAllocated
})

const canEditBudget = computed(() => {
  return selectedProject.value?.status === '待编制预算'
})

function progressColor(rate) {
  if (rate < 80) return '#67C23A'
  if (rate <= 95) return '#E6A23C'
  return '#F56C6C'
}

function safeExecutionRate(rate) {
  return Math.min(100, Number(Number(rate || 0).toFixed(1)))
}

function safeAllocationRate(amount) {
  const total = selectedProject.value?.totalBudget || 0
  if (total <= 0) return 0
  return Math.min(100, Number(((amount / total) * 100).toFixed(1)))
}

async function loadData() {
  const [projectRes, settingsRes, categoryRes] = await Promise.all([
    projectApi.getList(),
    settingsApi.getWarningThreshold(),
    categoryApi.getList()
  ])
  // 仅保留处于“待编制预算”状态的项目进行展示与编制
  projects.value = projectRes.data.filter(p => p.principalId === currentUser.id && p.status === '待编制预算')
  threshold.value = Number(settingsRes.data?.value || 95)
  categories.value = categoryRes.data || []
  
  if (projects.value.length) {
    // 优先从 URL 参数中获取项目 ID
    const routeProjectId = Number(route.query.projectId)
    if (routeProjectId && projects.value.some(p => p.id === routeProjectId)) {
      selectedProjectId.value = routeProjectId
    } else {
      selectedProjectId.value = projects.value[0].id
    }
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
  editing.budgetAmount = remainingAmount.value > 0 ? remainingAmount.value : 0
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
  await ElMessageBox.confirm('确定删除该科目预算吗？', '提示')
  await budgetApi.delete(id)
  ElMessage.success('已删除')
  await loadBudgets()
}

async function submit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (editing.budgetAmount > availableForEdit.value) {
      return ElMessage.error('该分项金额已超过项目剩余可用额度')
    }

    submitting.value = true
    try {
      if (isEdit.value) {
        await budgetApi.update(editing)
        ElMessage.success('预算修改完毕')
      } else {
        await budgetApi.add(editing)
        ElMessage.success('预算项添加成功')
      }
      visible.value = false
      await loadBudgets()
    } catch (err) {
      ElMessage.error(err.response?.data?.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function handleFinalConfirm() {
  if (!selectedProjectId.value) return
  if (remainingAmount.value !== 0) {
    return ElMessage.warning('预算尚未分配完全，请将立项金额全部分配后再启动。')
  }

  try {
    await ElMessageBox.confirm(
      '确认预算编制已完成并正式启动项目吗？启动后预算分配将被锁定。',
      '启动确认',
      { confirmButtonText: '立即启动', type: 'success' }
    )
    
    activating.value = true
    await projectApi.confirmBudget(selectedProjectId.value)
    ElMessage.success('项目已正式启动，进入执行阶段')
    
    // 跳转回项目列表
    router.push('/projects')
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '激活失败')
    }
  } finally {
    activating.value = false
  }
}

function initChart() {
  if (!chartRef.value || myChart) return
  
  // 检查容器是否有实际支出尺寸（防止在某些布局抖动瞬间 clientWidth/Height 为 0）
  const { clientWidth, clientHeight } = chartRef.value
  if (clientWidth === 0 || clientHeight === 0) {
    // 如果尺寸暂未就绪，延迟 100ms 重试，直到容器尺寸生效
    setTimeout(initChart, 100)
    return
  }
  
  myChart = echarts.init(chartRef.value)
  
  // 核心逻辑：允许悬浮高亮，但点击时强制设为“选中”状态，从而实现“点不动”的效果
  myChart.on('legendselectchanged', (params) => {
    myChart.setOption({
      legend: { selected: { [params.name]: true } }
    })
  })
  
  updateChart()
  // 立即强制触发一次 resize 确保填满容器
  setTimeout(() => {
    myChart && myChart.resize()
  }, 50)
}

function updateChart() {
  if (!myChart) return
  
  const option = {
    title: {
      text: '预算分配',
      left: 'center',
      top: 'center',
      textStyle: { fontSize: 18, fontWeight: 'normal', color: '#64748b' }
    },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical',
      left: '5%',
      bottom: '5%',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 14, color: '#64748b' },
      selectedMode: true
    },
    // 16种高对比度颜色
    color: [
      '#3b82f6', '#10b981', '#6366f1', '#8b5cf6', '#ec4899', '#14b8a6', '#f43f5e',
      '#f97316', '#06b6d4', '#84cc16', '#a855f7', '#facc15', '#2dd4bf', '#fb7185',
      '#60a5fa', '#34d399'
    ],
    series: [
      {
        name: '预算构成',
        type: 'pie',
        radius: ['55%', '80%'], 
        avoidLabelOverlap: false,
        center: ['50%', '50%'], 
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: false } },
        data: [
          ...budgets.value.map(b => ({
            name: b.categoryName,
            value: b.budgetAmount
          })),
          ...(remainingAmount.value > 0 ? [{
            name: '待分配余额',
            value: remainingAmount.value,
            itemStyle: { color: '#e2e8f0' }
          }] : [])
        ]
      }
    ]
  }
  myChart.setOption(option)
}

watch([allocatedTotal, remainingAmount], updateChart)

onMounted(async () => {
  await loadData()
  // 确保 DOM 已经根据数据渲染并计算出宽度高度后，再进行初始化
  setTimeout(() => {
    initChart()
    window.addEventListener('resize', handleResize)
  }, 300) // 给予充足的布局缓冲时间
})

function handleResize() {
  if (myChart) {
    myChart.resize()
  }
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
})
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.project-selector { display: flex; flex-direction: column; gap: 16px; }
.project-selector :deep(.el-form-item__label) { font-size: 16px; font-weight: 600; color: #1e293b; }
.project-selector :deep(.el-select .el-input__inner) { font-size: 15px; }
.project-meta { background: #f8fafc; border-radius: 8px; overflow: hidden; }
.meta-value { font-weight: 600; color: #1e293b; }
.summary-section { margin-bottom: 24px; }
.metrics-container { display: flex; flex-direction: column; height: 100%; }
.metrics-row { flex: 1; min-height: 0; }
.metrics-row :deep(.el-col) { display: flex; flex-direction: column; }
.summary-cards { margin-bottom: 0; }
.summary-card { height: 100%; width: 100%; border-radius: 12px; }
.summary-card.total { border-top: 4px solid #3b82f6; }
.summary-card.allocated { border-top: 4px solid #10b981; }
.summary-card.remaining { border-top: 4px solid #f59e0b; }
.summary-card :deep(.el-card__header) { padding: 12px 16px; font-size: 14px; font-weight: 600; background: #fbfcfe; border-bottom: 1px solid #f1f5f9; }
.amount { font-size: 26px; font-weight: 700; margin-top: 12px; font-family: 'JetBrains Mono', 'Courier New', Courier, monospace; letter-spacing: -0.5px; }
.text-success { color: #10b981; }
.text-danger { color: #ef4444; }

.currency-font { font-family: 'JetBrains Mono', 'Courier New', Courier, monospace; font-weight: 600; font-size: 18px; color: #1e293b; }

.status-title { font-size: 20px; font-weight: 700; }
.status-title.success { color: #10b981; }
.status-title.warning { color: #f59e0b; }

.progress-details { display: flex; flex-direction: column; gap: 6px; padding: 4px 0; }
.progress-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2px; }
.progress-label { font-size: 15px; color: #64748b; }
.progress-label strong { color: #0f172a; }

.specs { padding-left: 20px; font-size: 13px; color: #475569; line-height: 1.6; }
.form-tip { font-size: 12px; color: #64748b; margin-top: 4px; }
.chart-card { height: 100%; border-radius: 12px; }
.chart-card :deep(.el-card__body) { padding: 0 !important; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.allocation-chart { width: 100%; height: 260px; position: relative; }
.summary-section :deep(.el-card__body) { padding: 16px !important; height: 100%; box-sizing: border-box; }

:deep(.el-table) { font-size: 16px; }
</style>
