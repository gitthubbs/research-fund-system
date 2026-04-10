<template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup>
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent
} from 'echarts/components'
import * as echarts from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

echarts.use([
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer
])

const props = defineProps({
  option: {
    type: Object,
    required: true
  }
})

const chartRef = ref(null)
let instance = null

function renderChart() {
  if (!chartRef.value) return
  if (!instance) {
    instance = echarts.init(chartRef.value)
  }
  instance.setOption(props.option, true)
}

function handleResize() {
  instance?.resize()
}

watch(
  () => props.option,
  () => renderChart(),
  { deep: true }
)

onMounted(() => {
  renderChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  instance?.dispose()
  instance = null
})
</script>

<style scoped>
.chart {
  width: 100%;
  height: 320px;
}
</style>
