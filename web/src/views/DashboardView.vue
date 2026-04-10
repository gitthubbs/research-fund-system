<template>
  <div class="page">
    <section class="hero">
      <div>
        <p class="eyebrow">{{ roleLabel(user?.role) }}工作台</p>
        <h1>科研经费管理看板</h1>
        <p class="subtitle">围绕项目执行率、分类占比与月度趋势做统一监控。</p>
      </div>
    </section>

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

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <ChartCard title="项目执行率" subtitle="预算执行比例" :loading="loading">
          <EChartsPanel v-if="projectExecution.length" :option="barOption" />
          <el-empty v-else description="暂无数据" />
        </ChartCard>
      </el-col>
      <el-col :xs="24" :lg="12">
        <ChartCard title="分类支出占比" subtitle="按分类金额统计" :loading="loading">
          <EChartsPanel v-if="categoryShare.length" :option="pieOption" />
          <el-empty v-else description="暂无数据" />
        </ChartCard>
      </el-col>
    </el-row>

    <ChartCard title="月度支出趋势" subtitle="按月份聚合" :loading="loading">
      <EChartsPanel v-if="monthlyTrend.length" :option="lineOption" />
      <el-empty v-else description="暂无数据" />
    </ChartCard>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { statisticsApi } from '@/api/statisticsApi'
import EChartsPanel from '@/components/charts/EChartsPanel.vue'
import ChartCard from '@/components/charts/ChartCard.vue'
import { getUser } from '@/utils/auth'
import { roleLabel } from '@/utils/format'

const loading = ref(false)
const user = ref(getUser())

// ★ 新增
const projectExecution = ref([])
// ★ 新增
const categoryShare = ref([])
// ★ 新增
const monthlyTrend = ref([])

// ★ 新增
const overview = computed(() => {
  const projectCount = projectExecution.value.length
  const totalExpenditure = categoryShare.value.reduce((sum, item) => sum + Number(item.value || 0), 0)
  const avgExecution = projectCount
    ? projectExecution.value.reduce((sum, item) => sum + Number(item.value || 0), 0) / projectCount
    : 0
  return [
    { label: '项目数量', value: projectCount },
    { label: '总支出', value: totalExpenditure, type: 'currency', precision: 2 },
    { label: '平均执行率', value: avgExecution, precision: 2, suffix: '%' }
  ]
})

// ★ 修改
async function loadSummary() {
  loading.value = true
  try {
    const [projectExecutionResp, categoryResp, monthlyResp] = await Promise.all([
      statisticsApi.getProjectExecution(),
      statisticsApi.getCategoryShare(),
      statisticsApi.getMonthly()
    ])

    projectExecution.value = (projectExecutionResp.data || []).map((item) => ({
      name: item.projectName,
      value: Number(item.executionRate || 0) * 100
    }))

    categoryShare.value = (categoryResp.data || []).map((item) => ({
      name: item.categoryName,
      value: Number(item.amount || 0)
    }))

    monthlyTrend.value = (monthlyResp.data || []).map((item) => ({
      month: item.month,
      amount: Number(item.amount || 0)
    }))
  } finally {
    loading.value = false
  }
}

// ★ 新增
const barOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    valueFormatter: (value) => `${Number(value).toFixed(2)}%`
  },
  grid: { left: 20, right: 20, top: 20, bottom: 48, containLabel: true },
  xAxis: {
    type: 'category',
    axisLabel: { rotate: 25 },
    data: projectExecution.value.map((item) => item.name)
  },
  yAxis: {
    type: 'value',
    max: 100,
    axisLabel: { formatter: '{value}%' }
  },
  series: [
    {
      type: 'bar',
      data: projectExecution.value.map((item) => item.value),
      barWidth: 28,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#0ea5e9' },
            { offset: 1, color: '#2563eb' }
          ]
        },
        borderRadius: [8, 8, 0, 0]
      }
    }
  ]
}))

const lineOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    valueFormatter: (value) => `¥${Number(value).toLocaleString('zh-CN')}`
  },
  grid: {
    left: 20,
    right: 20,
    top: 20,
    bottom: 28,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: monthlyTrend.value.map((item) => item.month),
    axisLine: { lineStyle: { color: '#cbd5e1' } }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#e2e8f0' } },
    axisLabel: {
      formatter: (value) => `${Math.round(value / 1000)}k`
    }
  },
  series: [
    {
      type: 'line',
      smooth: true,
      data: monthlyTrend.value.map((item) => item.amount),
      symbolSize: 8,
      lineStyle: { color: '#2563eb', width: 3 },
      itemStyle: { color: '#1d4ed8' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(37, 99, 235, 0.28)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.02)' }
          ]
        }
      }
    }
  ]
}))

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
      radius: ['48%', '72%'],
      center: ['50%', '44%'],
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        formatter: '{b}\n{d}%'
      },
      data: categoryShare.value
    }
  ]
}))

onMounted(loadSummary)
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.hero {
  padding: 28px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.24), transparent 28%),
    linear-gradient(135deg, #f8fbff, #eef5ff);
}

.eyebrow {
  color: #1d4ed8;
  font-weight: 600;
  margin-bottom: 8px;
}

.subtitle {
  color: #64748b;
  margin-top: 10px;
}

.stats-row {
  margin-bottom: 0;
}

.metric-card {
  border-radius: 20px;
}
</style>
