<template>
  <div class="page">
    <div class="summary">
      <div>
        <p class="eyebrow">项目详情</p>
        <div class="title-row">
          <h2>{{ project.projectName }}</h2>

        </div>
        <p class="muted">{{ project.projectCode }} · 负责人 {{ project.principalName }}</p>
      </div>
      <div class="summary-extra">
        <div class="total-progress">
          <p class="progress-label">总执行进度：{{ totalExecution.actualRate }}%</p>
          <el-progress 
            :percentage="Number(totalExecution.rate)" 
            :color="getCustomColor(totalExecution.actualRate)"
            :stroke-width="12"
            style="width: 200px"
          />
        </div>
      </div>
    </div>

    <el-card shadow="never" class="milestone-card">
      <el-steps :active="activeStep" align-center>
        <el-step
          v-for="item in project.milestones"
          :key="item.stage"
          :title="item.stage"
          :description="item.content"
          :status="item.type === 'danger' ? 'error' : undefined"
        >
          <template #title>
            <div class="step-title">
              <span>{{ item.stage }}</span>
              <span class="step-date">{{ item.date }}</span>
            </div>
          </template>
        </el-step>
      </el-steps>
    </el-card>
    
    <!-- ★ 新增：逾期结题强提醒 -->
    <el-alert
      v-if="project.status === 4 && isOverdue"
      title="项目执行已逾期"
      type="warning"
      description="当前项目已达到结束日期，建议您尽快核实支出数据并提交结题审计。"
      show-icon
      :closable="false"
      effect="dark"
    />

    <!-- ★ 修改：仅执行中或已结题的项目显示偏移分析 -->
    <el-card shadow="never" class="analysis-card" v-if="project.status >= 4 && project.startDate && project.endDate">
      <template #header>
        <div class="card-header">
          <span>执行偏移分析</span>
        </div>
      </template>
      
      <div class="analysis-content">
        <div class="analysis-item">
          <div class="analysis-label">时间进度 (Time)</div>
          <el-progress :percentage="timeProgress" color="#94a3b8" />
          <p class="analysis-desc">项目周期：{{ project.startDate }} 至 {{ project.endDate }}</p>
        </div>
        <div class="analysis-item">
          <div class="analysis-label">经费执行 (Fund)</div>
          <el-progress :percentage="Number(totalExecution.rate)" :color="getCustomColor(totalExecution.actualRate)" />
          <p class="analysis-desc">当前实际执行率：{{ totalExecution.actualRate }}%</p>
        </div>
      </div>

      <el-alert
        v-if="analysisResult.isLagging"
        :title="analysisResult.alertTitle"
        :type="analysisResult.alertType"
        show-icon
        :closable="false"
        effect="dark"
        style="margin-top: 16px"
      >
        <p>{{ analysisResult.alertDesc }}</p>
      </el-alert>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>预算执行明细 (项目总额：{{ formatCurrency(totalExecution.budget) }})</span>
          <div class="actions">
            <el-button 
              v-if="project.status === 4 && user.role !== 'admin'"
              type="primary" 
              plain
              size="small"
              @click="router.push({ path: '/expenditures/add', query: { projectId: project.id } })"
            >去报销录入</el-button>
            <el-button 
              v-if="project.status === 4 && user.role !== 'admin'"
              type="success" 
              size="small"
              @click="handleFinish"
            >申请结题</el-button>
          </div>
        </div>
      </template>
      <el-table :data="project.budgets" empty-text="暂无数据">
        <el-table-column prop="categoryName" label="预算科目" width="180" />
        <el-table-column label="预算金额">
          <template #default="{ row }">{{ formatCurrency(row.budgetAmount) }}</template>
        </el-table-column>
        <el-table-column label="已支出">
          <template #default="{ row }">{{ formatCurrency(row.spentAmount) }}</template>
        </el-table-column>
        <el-table-column label="执行进度" min-width="200">
          <template #default="{ row }">
            <div class="table-progress">
              <el-progress 
                :percentage="Math.min(Number(row.executionRate), 100)" 
                :color="getCustomColor(row.executionRate)"
                :format="() => formatPercent(row.executionRate)"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus' // ★ 新增
import { projectApi } from '@/api/projectApi'
import { formatCurrency, formatPercent } from '@/utils/format'
import { getUser } from '@/utils/auth' // ★ 新增

const route = useRoute()
const router = useRouter() // ★ 修改
const user = getUser() || {} // ★ 新增获取当前用户
const project = reactive({
  projectName: '',
  projectCode: '',
  principalName: '',
  status: 0,      // ★ 新增
  startDate: '', // ★ 新增
  endDate: '',   // ★ 新增
  milestones: [],
  budgets: []
})



// ★ 新增：计算当前进度索引
const activeStep = computed(() => {
  if (!project.milestones || project.milestones.length === 0) return 0
  // 增加 'warning' 类型核心识别，确保逾期状态能正确识别为当前步骤
  const index = project.milestones.findIndex((m) => ['primary', 'danger', 'warning'].includes(m.type))
  if (index !== -1) return index
  // 如果全是 success，则高亮全部（返回 4 或更高）
  if (project.milestones.every((m) => m.type === 'success')) return project.milestones.length
  return 0
})

// ★ 新增：时间进度百分比
const timeProgress = computed(() => {
  if (!project.startDate || !project.endDate) return 0
  const start = new Date(project.startDate).getTime()
  const end = new Date(project.endDate).getTime()
  const now = new Date().getTime()

  if (now <= start) return 0
  if (now >= end) return 100

  return Math.round(((now - start) / (end - start)) * 100)
})

// ★ 新增：判断是否逾期
const isOverdue = computed(() => {
  if (!project.endDate || project.status !== 4) return false
  const end = new Date(project.endDate).getTime()
  const now = new Date().getTime()
  // 如果当前时间 >= 结束日期（或当天），判定为逾期
  return now >= end
})

// ★ 新增：偏移分析结果
const analysisResult = computed(() => {
  // ★ 新增：非执行/结题状态不进行分析
  if (!project.status || project.status < 4) {
    return { isLagging: false, status: '暂未开始执行' }
  }

  const fundRate = Number(totalExecution.value.actualRate)
  const timeRate = timeProgress.value
  const diff = timeRate - fundRate

  let status = '进度匹配良好'
  let type = 'success'
  let isLagging = false
  let alertTitle = ''
  let alertDesc = ''

  if (diff > 50) {
    status = '进度严重滞后'
    type = 'error'
    isLagging = true
    alertTitle = '严重执行偏差预警'
    alertDesc = '经费执行严重滞后，已落后时间进度 50% 以上，请立即处理支出或进行预算调整。'
  } else if (diff > 30) {
    status = '进度执行滞后'
    type = 'warning'
    isLagging = true
    alertTitle = '执行进度预警'
    alertDesc = '经费执行滞后，请注意支出进度。当前资金支出显著慢于时间进度。'
  } else if (diff < -50) {
    status = '经费严重超前'
    type = 'error'
    isLagging = true
    alertTitle = '经费支出异常提醒'
    alertDesc = '当前经费执行进度已大幅领先于时间进度（超过 50%），请核实是否存在提前列支或超预算执行风险。'
  } else if (diff < -30) {
    status = '经费支出偏快'
    type = 'warning'
    isLagging = true
    alertTitle = '进度偏移预警'
    alertDesc = '经费执行进度快于时间进度。当前资金支出显著超前，请合理规划后续经费使用。'
  }

  return {
    isLagging,
    diff: diff.toFixed(1),
    status,
    alertType: type,
    tagType: type === 'error' ? 'danger' : type,
    alertTitle,
    alertDesc
  }
})

// ★ 新增：进度条计算逻辑
const totalExecution = computed(() => {
  if (!project.budgets || project.budgets.length === 0) {
    return { spent: 0, budget: 0, rate: 0, actualRate: 0 }
  }
  const totalBudget = project.budgets.reduce((sum, b) => sum + (b.budgetAmount || 0), 0)
  const totalSpent = project.budgets.reduce((sum, b) => sum + (b.spentAmount || 0), 0)
  const rate = totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0
  return {
    spent: totalSpent,
    budget: totalBudget,
    rate: Math.min(rate, 100).toFixed(1),
    actualRate: rate.toFixed(1)
  }
})

const getCustomColor = (rate) => {
  if (rate >= 100) return '#F56C6C' // 红色：超支或满额
  if (rate >= 80) return '#E6A23C'  // 橙色：预警
  return '#67C23A'                 // 绿色：安全
}

async function loadDetail() {
  const response = await projectApi.getById(route.params.id)
  Object.assign(project, response.data)
}

// ★ 新增：结题处理
const handleFinish = () => {
  ElMessageBox.confirm(
    '确认项已完成所有任务并申请结题验收吗？申请将提交至管理员进行审核，期间将冻结经费操作。',
    '项目结题确认',
    {
      confirmButtonText: '提交结题申请',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await projectApi.finish(project.id)
      ElMessage.success('结题验收申请已提交')
      loadDetail()
    } catch (error) {
      ElMessage.error('操作失败，请重试')
    }
  }).catch(() => {})
}

onMounted(loadDetail)
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 8px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 4px;
}



.summary-extra {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-progress {
  text-align: right;
}

.progress-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
  font-weight: 600;
}

.eyebrow {
  color: #1d4ed8;
  font-weight: 600;
  margin-bottom: 4px;
}

.muted {
  color: #64748b;
  font-size: 14px;
}

.milestone-card {
  padding: 12px 0;
}

.analysis-card {
  margin-top: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.analysis-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  padding: 8px 0;
}

.analysis-label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 12px;
}

.analysis-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

.step-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.2;
}

.step-date {
  font-size: 12px;
  font-weight: normal;
  color: #94a3b8;
  margin-top: 4px;
}

:deep(.el-step__title) {
  text-align: center;
}

:deep(.el-step__description) {
  text-align: center;
  padding-left: 20%;
  padding-right: 20%;
  font-size: 13px;
}
</style>
