<template>
  <div class="page">
    <!-- 1. 英雄区 -->
    <section class="hero">
      <div class="hero-content">
        <div>
          <p class="eyebrow">{{ roleLabel(user?.role) }}工作台</p>
          <h1>{{ currentDashboardTitle }}</h1>
          <p class="subtitle">{{ dashboardSubtitle }}</p>
        </div>
        <div class="hero-actions">
          <!-- ★ 新增：管理员人员切换器 -->
          <div v-if="user?.role === 'admin'" class="researcher-selector">
            <span class="selector-label">监控视角：</span>
            <el-select 
              v-model="selectedResearcherId" 
              placeholder="全校概览 (快照)" 
              clearable 
              filterable
              @change="handleResearcherChange"
              style="width: 220px"
              class="custom-select"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
              <el-option label="全校总览 (全局)" :value="null" />
              <el-option
                v-for="item in researcherList"
                :key="item.id"
                :label="item.name + ' (' + item.department + ')'"
                :value="item.id"
              />
            </el-select>
          </div>

          <div v-if="user?.role === 'admin' && !selectedResearcherId && statsOverview.snapshotTime" class="update-time">
            最后更新于: {{ statsOverview.snapshotTime }}
          </div>
          <el-button v-if="user?.role === 'admin' && !selectedResearcherId" type="primary" :loading="syncLoading" :icon="Refresh" @click="manualSync">
            同步快照数据
          </el-button>
        </div>
      </div>
    </section>

    <!-- 2. 主统计区 -->
    <div class="dashboard-main">
      <el-row :gutter="16" class="stats-row">
        <el-col v-for="item in overview" :key="item.label" :xs="24" :sm="12" :lg="8">
          <el-card shadow="hover" class="metric-card">
            <el-statistic :title="item.label" :value="item.value" :precision="item.precision || 0">
              <template v-if="item.type === 'currency'" #prefix>¥</template>
              <template v-if="item.suffix" #suffix>{{ item.suffix }}</template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- ★ 新增：全局风险监控概览（仅管理员全校视图可见） -->
      <div v-if="user?.role === 'admin' && !selectedResearcherId" class="risk-dashboard">
        <div class="section-title">全校执行风险监控</div>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <div class="risk-card danger" @click="router.push('/projects')">
              <div class="risk-icon"><el-icon><WarningFilled /></el-icon></div>
              <div class="risk-info">
                <div class="risk-value">{{ globalRisk.overdueProjects || 0 }}</div>
                <div class="risk-label">已逾期未结题项目</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="risk-card warning" @click="router.push('/projects')">
              <div class="risk-icon"><el-icon><Timer /></el-icon></div>
              <div class="risk-info">
                <div class="risk-value">{{ globalRisk.alertProjects || 0 }}</div>
                <div class="risk-label">临近到期项目 (20天内)</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="risk-card info" @click="router.push('/projects')">
              <div class="risk-icon"><el-icon><TrendCharts /></el-icon></div>
              <div class="risk-info">
                <div class="risk-value">{{ globalRisk.laggingProjects || 0 }}</div>
                <div class="risk-label">严重执行滞后项目 (偏差>30%)</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-row :gutter="16" style="margin-top: 20px">
        <!-- ★ 图表优化：横向双维度对比图 -->
        <el-col :xs="24" :lg="16">
          <ChartCard 
            :title="chartTitleProject" 
            :subtitle="chartSubtitleProject" 
            :loading="loading">
            <template #extra v-if="user?.role === 'admin' && !selectedResearcherId">
               <el-button link type="primary" @click="router.push('/projects')">查看全部项目 <el-icon><Right /></el-icon></el-button>
            </template>
            <EChartsPanel 
              v-if="projectExecution.length" 
              :option="barOption" 
              @item-click="handleChartClick" 
            />
            <el-empty v-else description="暂无项目数据" />
          </ChartCard>
        </el-col>
        
        <el-col :xs="24" :lg="8">
          <ChartCard title="分类支出占比" subtitle="按分类金额统计" :loading="loading">
            <EChartsPanel v-if="categoryShare.length" :option="pieOption" />
            <el-empty v-else description="暂无数据" />
          </ChartCard>
        </el-col>
      </el-row>

      <div style="margin-top: 20px">
        <ChartCard title="月度支出趋势" subtitle="历史支出与未来预测" :loading="loading">
          <EChartsPanel v-if="trendData.months.length" :option="lineOption" />
          <el-empty v-else description="暂无历史数据" />
          
          <div v-if="trendData.months.length" class="decision-box">
            <el-alert
              v-if="trendData.forecast?.isAlert"
              title="支出频率预警：当前月均消耗率高于立项计划，建议关注余额。"
              type="warning"
              show-icon
              :closable="false"
            />
            <el-alert
              v-if="isLagging"
              title="执行偏差提醒：部分项目经费执行显著滞后于时间进度，建议关注执行效率。"
              type="info"
              show-icon
              :closable="false"
              style="margin-top: 8px"
            />
          </div>
        </ChartCard>
      </div>
    </div>

    <!-- 3. 悬浮助手触发球 (仅科研人员) -->
    <div 
      v-if="user?.role === 'researcher'" 
      class="float-assistant-trigger"
      :class="{ 'has-advice': advices.length > 0 }"
      @click="drawerVisible = true"
    >
      <el-badge :value="advices.length || ''" :hidden="!advices.length" class="badge-item">
        <div class="ai-sphere">
          <el-icon><MagicStick /></el-icon>
        </div>
      </el-badge>
      <span class="trigger-label">智能助手</span>
    </div>

    <!-- 4. 助手抽屉面板 -->
    <el-drawer
      v-model="drawerVisible"
      title="智能助手建议"
      direction="rtl"
      size="380px"
      append-to-body
      class="assistant-drawer"
    >
      <template #header>
        <div class="drawer-header">
          <el-icon class="ai-icon"><MagicStick /></el-icon>
          <span>智能助手</span>
          <el-tag size="small" type="success" effect="plain" round style="margin-left: 10px">扫描中</el-tag>
        </div>
      </template>

      <div v-loading="adviceLoading" class="advice-list">
        <template v-if="groupedAdvices.length">
          <!-- ★ 聚合卡片设计 -->
          <div 
            v-for="(group, gIdx) in groupedAdvices" 
            :key="gIdx" 
            class="advice-group-card"
            :class="[group.type, { 'is-expanded': expandedGroups.includes(group.title) }]"
          >
            <div class="group-header" @click="toggleGroup(group.title)">
              <div class="group-title-info">
                <el-icon v-if="group.type === 'danger'"><WarningFilled /></el-icon>
                <el-icon v-else-if="group.type === 'warning'"><BellFilled /></el-icon>
                <el-icon v-else><InfoFilled /></el-icon>
                <span class="title-text">{{ group.title }}</span>
              </div>
              <div class="group-header-right">
                <el-badge 
                  :value="group.items.length" 
                  :hidden="group.items.length <= 1" 
                  type="danger" 
                  class="group-badge"
                />
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>

            <!-- 展开详情区域 -->
            <el-collapse-transition>
              <div v-show="expandedGroups.includes(group.title)" class="group-details">
                <div 
                  v-for="(item, iIdx) in group.items" 
                  :key="iIdx" 
                  class="detail-item"
                  @click="handleAction(item.link)"
                >
                  <div class="detail-dot"></div>
                  <div class="detail-content">{{ item.content }}</div>
                  <el-icon class="jump-icon"><Right /></el-icon>
                </div>
                <!-- 底部交互：仅在聚合场景下显示统一已读 -->
                <div v-if="group.items[0].businessType" class="group-footer">
                  <el-button link type="info" size="small" @click.stop="markRead(group.items[0])">
                    <el-icon><CircleCheck /></el-icon>全部标记为已读
                  </el-button>
                </div>
              </div>
            </el-collapse-transition>
          </div>
        </template>
        <el-empty v-else description="暂无潜在风险，经费运行良好" />
      </div>
      
      <template #footer>
        <div class="drawer-footer">
          建议仅供参考，请以最新财务通知为准
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Refresh, MagicStick, WarningFilled, 
  BellFilled, InfoFilled, CircleCheck,
  ArrowRight, Right, User, Timer, TrendCharts
} from '@element-plus/icons-vue'
import { statisticsApi } from '@/api/statisticsApi'
import { userApi } from '@/api/userApi'
import EChartsPanel from '@/components/charts/EChartsPanel.vue'
import ChartCard from '@/components/charts/ChartCard.vue'
import { getUser } from '@/utils/auth'
import { roleLabel } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const adviceLoading = ref(false)
const drawerVisible = ref(false)
const user = ref(getUser())

// 数据存储
const projectExecution = ref([])
const categoryShare = ref([])
const advices = ref([])
const trendData = ref({ months: [], actualData: [], forecast: {}, baseline: 0 })
const statsOverview = ref({ projectCount: 0, totalSpent: 0, rate: 0, snapshotTime: '' })
const globalRisk = ref({ overdueProjects: 0, laggingProjects: 0, alertProjects: 0 })

// 交互状态
const expandedGroups = ref([])
const selectedResearcherId = ref(null)
const researcherList = ref([])

const currentDashboardTitle = computed(() => {
  if (!selectedResearcherId.value) return '科研经费管理看板'
  const researcher = researcherList.value.find(r => r.id === selectedResearcherId.value)
  return `${researcher?.name || '未知人员'} 的科研面板`
})

const dashboardSubtitle = computed(() => {
  if (selectedResearcherId.value) return '正在以科研负责人视角查看其实时经费执行情况。'
  return user.value?.role === 'admin' 
    ? '全局宏观数据与核心高风险异常预警系统' 
    : '查看您负责的项目预算执行情况与月度支出趋势。'
})

const chartTitleProject = computed(() => {
  return (!selectedResearcherId.value && user.value?.role === 'admin') 
    ? '异常监控：重点关注项目 (执行异常 Top 10)'
    : '项目执行与进度对比'
})

const chartSubtitleProject = computed(() => {
  return (!selectedResearcherId.value && user.value?.role === 'admin') 
    ? '智能筛选时间进度与经费支出偏差最大的高风险项目'
    : '决策支持：对比经费执行率与自然时间进度'
})

const isLagging = computed(() => {
  if (!projectExecution.value.length) return false
  const avgTimeProgress = projectExecution.value.reduce((acc, cur) => acc + cur.timeProgress, 0) / projectExecution.value.length
  // 只有当平均时间进度超过 10% 时，且总执行率低于 30%，才判定为滞后
  return avgTimeProgress > 10 && statsOverview.value.rate < 30
})
const overview = computed(() => [
  { label: '项目数量', value: statsOverview.value.projectCount },
  { label: '累计总支出', value: statsOverview.value.totalSpent, type: 'currency', precision: 2 },
  { label: '平均执行率', value: statsOverview.value.rate, precision: 2, suffix: '%' }
])

// ★ 计算聚合后的建议
const groupedAdvices = computed(() => {
  const groups = {}
  advices.value.forEach(ad => {
    if (!groups[ad.title]) {
      groups[ad.title] = { 
        title: ad.title, 
        type: ad.type, 
        items: [] 
      }
    }
    groups[ad.title].items.push(ad)
  })
  return Object.values(groups)
})

// 加载数据
async function loadSummary() {
  loading.value = true
  const rid = selectedResearcherId.value
  try {
    const promises = [
      statisticsApi.getOverview(rid),
      statisticsApi.getProjectExecution(rid),
      statisticsApi.getCategoryShare(rid),
      statisticsApi.getMonthly(rid)
    ]
    if (user.value?.role === 'admin' && !rid) {
      promises.push(statisticsApi.getGlobalRisk())
    } else {
      promises.push(Promise.resolve({ data: {} }))
    }

    const [overviewResp, projectResp, categoryResp, monthlyResp, riskResp] = await Promise.all(promises).catch(() => [{}, {}, {}, {}, {}])

    statsOverview.value = overviewResp.data || { projectCount: 0, totalSpent: 0, rate: 0, snapshotTime: '' }
    
    // 全局风险数据
    if (user.value?.role === 'admin' && !rid && riskResp.data) {
      globalRisk.value = riskResp.data
    }
    
    // 注入多维数据
    projectExecution.value = (projectResp.data || []).reverse().map(i => ({
      name: i.projectName,
      value: Number(i.rate || 0),
      projectId: i.projectId,
      timeProgress: Number(i.timeProgress || 0)
    }))

    categoryShare.value = (categoryResp.data || []).map(i => ({ name: i.categoryName, value: Number(i.amount || 0) }))
    trendData.value = monthlyResp.data || { months: [], actualData: [], forecast: {}, baseline: 0 }
  } finally {
    loading.value = false
  }
}

async function loadAdvices() {
  // 管理员在选择了科研人员时，也可以查看该人员的助手建议
  if (user.value?.role !== 'researcher' && !selectedResearcherId.value) return
  
  adviceLoading.value = true
  try {
    const res = await statisticsApi.getAssistantAdvice(selectedResearcherId.value)
    advices.value = res.data || []
    // 默认展开第一个有内容的组
    if (advices.value.length > 0) {
      const firstTitle = advices.value[0].title
      if (!expandedGroups.value.includes(firstTitle)) {
        expandedGroups.value.push(firstTitle)
      }
    }
  } finally {
    adviceLoading.value = false
  }
}

async function fetchResearchers() {
  if (user.value?.role !== 'admin') return
  try {
    const res = await userApi.getList()
    researcherList.value = res.data.filter(u => u.role === 'researcher')
  } catch (e) { console.error('Failed to fetch researchers', e) }
}

function handleResearcherChange() {
  loadSummary()
  loadAdvices()
}

// 交互逻辑
function toggleGroup(title) {
  const index = expandedGroups.value.indexOf(title)
  if (index > -1) {
    expandedGroups.value.splice(index, 1)
  } else {
    expandedGroups.value.push(title)
  }
}

function handleChartClick(params) {
  const data = projectExecution.value[params.dataIndex]
  if (data?.projectId) router.push(`/projects/${data.projectId}`)
}

function handleAction(link) {
  if (!link) return
  drawerVisible.value = false
  router.push(link)
}

async function markRead(advice) {
  try {
    await statisticsApi.markAdviceRead(advice.businessType)
    await loadAdvices()
    ElMessage.success('已标记')
  } catch (e) { ElMessage.error('操作失败') }
}

async function manualSync() {
  syncLoading.value = true
  try {
    await statisticsApi.saveSnapshot(0)
    await loadSummary()
    ElMessage.success('统计已同步')
  } finally { syncLoading.value = false }
}

const syncLoading = ref(false)

// ★ 图表配置优化：决策支持型条形图
const barOption = computed(() => {
  const names = projectExecution.value.map(i => i.name)
  const rates = projectExecution.value.map(i => i.value)
  const progresses = projectExecution.value.map(i => i.timeProgress)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const rate = params[1].value
        const progress = params[0].value
        const color = rate > progress ? '#ef4444' : '#10b981'
        return `<b>${params[0].name}</b><br/>
                经费执行率: <span style="color:${color}">${rate}%</span><br/>
                项目进度比: ${progress}%<br/>
                <small style="color:#94a3b8">点击可查看预算明细</small>`
      }
    },
    legend: {
      show: true,
      bottom: 0,
      data: ['经费执行率', '预计时间进度'],
      textStyle: { color: '#64748b', fontSize: 12 }
    },
    grid: { left: 20, right: 80, top: 20, bottom: 40, containLabel: true },
    xAxis: { type: 'value', max: (v) => Math.max(100, v.max), splitLine: { lineStyle: { type: 'dashed' } } },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        width: 140, overflow: 'truncate', interval: 0,
        formatter: (val) => val.length > 12 ? val.slice(0, 11) + '...' : val
      }
    },
    series: [
      {
        name: '预计时间进度',
        type: 'bar',
        data: progresses,
        barWidth: 28, // 较宽的背景条
        itemStyle: {
          color: 'rgba(203, 213, 225, 0.5)',
          borderRadius: [0, 4, 4, 0]
        },
        z: 1,
        silent: true // 背景条不触发事件
      },
      {
        name: '经费执行率',
        type: 'bar',
        data: rates,
        barWidth: 12, // 较细的彩色条，叠放在宽条中心
        barGap: '-72%', // 关键：利用负间隔实现中心重叠
        z: 3,
        label: { 
          show: true, 
          position: 'right', 
          formatter: '{c}%', 
          color: '#1e293b', 
          fontWeight: 'bold',
          distance: 10
        },
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: (params) => {
            const val = params.value
            if (val >= 100) return '#F56C6C' 
            if (val >= 80) return '#E6A23C'
            return '#67C23A'
          }
        }
      }
    ]
  }
})

// Line & Pie remains updated from previous logic ...
const lineOption = computed(() => { /* ... similar level of detail ... */ 
    const { months, actualData, forecast, baseline } = trendData.value
    if (!months?.length) return {}
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 40, top: 40, bottom: 20 },
      xAxis: { type: 'category', boundaryGap: false, data: [...months, forecast.nextMonth] },
      yAxis: { type: 'value' },
      series: [
        { name: '实际支出', type: 'line', smooth: true, data: actualData, areaStyle: { color: 'rgba(37, 99, 235, 0.1)' }, lineStyle: { width: 3 } },
        { name: '预测支出', type: 'line', smooth: true, data: [...Array(actualData.length - 1).fill(null), actualData[actualData.length-1], forecast.value], lineStyle: { type: 'dashed' } }
      ],
      markLine: { data: [{ yAxis: baseline }], lineStyle: { color: '#f59e0b', type: 'dashed' } }
    }
})
const pieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: ({ name, value, percent }) => `${name}<br/>¥${Number(value).toLocaleString('zh-CN')} (${percent}%)`
  },
  legend: {
    bottom: 0,
    icon: 'circle'
  },
  color: ['#2563eb', '#f59e0b', '#10b981', '#ef4444', '#0ea5e9', '#e11d48'],
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '42%'],
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        formatter: '{b}\n{d}%',
        overflow: 'none', 
        edgeDistance: '5%'
      },
      data: categoryShare.value
    }
  ]
}))

onMounted(() => { 
  loadSummary(); 
  loadAdvices(); 
  fetchResearchers();
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 24px; }
.hero { padding: 32px; border-radius: 28px; background: linear-gradient(135deg, #f0f7ff, #e0f2fe); }
.eyebrow { color: #2563eb; font-weight: 700; margin-bottom: 8px; text-transform: uppercase; font-size: 13px; }
.metric-card { border-radius: 20px; border: none; }

.hero-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.researcher-selector {
  display: flex;
  align-items: center;
  background: rgba(37, 99, 235, 0.05);
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(37, 99, 235, 0.1);
}

.selector-label {
  font-size: 13px;
  font-weight: 600;
  color: #1e40af;
  margin-right: 12px;
}

.custom-select :deep(.el-input__wrapper) {
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

/* 全球风险卡片样式 */
.risk-dashboard {
  margin-top: 24px;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: #3b82f6;
  border-radius: 2px;
}
.risk-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #f1f5f9;
}
.risk-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
}
.risk-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}
.risk-info {
  flex: 1;
}
.risk-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}
.risk-label {
  font-size: 13px;
  color: #64748b;
}

.risk-card.danger .risk-icon { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
.risk-card.danger:hover { border-color: rgba(239, 68, 68, 0.3); }
.risk-card.danger .risk-value { color: #ef4444; }

.risk-card.warning .risk-icon { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.risk-card.warning:hover { border-color: rgba(245, 158, 11, 0.3); }
.risk-card.warning .risk-value { color: #f59e0b; }

.risk-card.info .risk-icon { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
.risk-card.info:hover { border-color: rgba(59, 130, 246, 0.3); }
.risk-card.info .risk-value { color: #3b82f6; }

.float-assistant-trigger { position: fixed; right: 40px; bottom: 40px; cursor: pointer; z-index: 2000; display: flex; flex-direction: column; align-items: center; gap: 8px; }
.ai-sphere { width: 60px; height: 60px; border-radius: 30px; background: linear-gradient(135deg, #8b5cf6, #6366f1); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 26px; box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3); }
.has-advice .ai-sphere::after { content: ''; position: absolute; width:100%; height:100%; border-radius:50%; border:3px solid #8b5cf6; animation: pulse 2s infinite; }
@keyframes pulse { 0% { transform: scale(1); opacity:0.6; } 100% { transform: scale(1.6); opacity:0; } }

.trigger-label { font-size: 12px; font-weight: 700; color: #6366f1; background: #fff; padding: 2px 8px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }

/* 助手聚合卡片样式 */
.advice-list { display: flex; flex-direction: column; gap: 14px; padding: 10px 0; }

.advice-group-card {
  border-radius: 16px; 
  border: 1px solid #f1f5f9;
  background: #fff;
  overflow: hidden;
  transition: all 0.3s ease;
}

.group-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.group-title-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 15px;
}

.title-text { color: #1e293b; }

.group-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.arrow-icon {
  font-size: 14px;
  color: #94a3b8;
  transition: transform 0.3s ease;
}

.is-expanded .arrow-icon { transform: rotate(90deg); }

/* 类型配色 */
.danger.advice-group-card { border-left: 5px solid #ef4444; }
.warning.advice-group-card { border-left: 5px solid #f59e0b; }
.info.advice-group-card { border-left: 5px solid #2563eb; }

.danger .group-title-info .el-icon { color: #ef4444; }
.warning .group-title-info .el-icon { color: #f59e0b; }
.info .group-title-info .el-icon { color: #2563eb; }

/* 详情列表 */
.group-details {
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
}

.detail-item {
  padding: 12px 16px 12px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f1f5f9;
}

.detail-item:last-of-type { border-bottom: none; }

.detail-item:hover { background: #f1f5f9; }

.detail-dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: #cbd5e1;
}

.detail-content {
  flex: 1;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}

.jump-icon {
  font-size: 14px;
  color: #3b82f6;
  opacity: 0;
  transition: opacity 0.2s;
}

.detail-item:hover .jump-icon { opacity: 1; }

.group-footer {
  padding: 8px 16px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  border-top: 1px solid #f1f5f9;
}

.drawer-footer { text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 15px; }

.decision-box { margin-top: 20px; }
</style>
