<template>
  <div class="pie-layout">
    <div class="donut" :style="{ background: gradient }">
      <div class="donut-hole">
        <strong>{{ total }}</strong>
        <span>总额</span>
      </div>
    </div>
    <div class="legend">
      <div v-for="(item, index) in normalized" :key="item.name" class="legend-item">
        <span class="dot" :style="{ backgroundColor: colors[index % colors.length] }"></span>
        <span>{{ item.name }}</span>
        <strong>{{ item.percent }}%</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const colors = ['#1f6feb', '#f59e0b', '#ef4444', '#10b981', '#8b5cf6']

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const total = computed(() => props.data.reduce((sum, item) => sum + Number(item.value || 0), 0))

const normalized = computed(() => {
  if (!total.value) return []
  return props.data.map((item) => ({
    ...item,
    percent: ((item.value / total.value) * 100).toFixed(1)
  }))
})

const gradient = computed(() => {
  if (!normalized.value.length) {
    return 'conic-gradient(#e2e8f0 0deg 360deg)'
  }
  let start = 0
  const stops = normalized.value.map((item, index) => {
    const end = start + Number(item.percent) * 3.6
    const color = colors[index % colors.length]
    const stop = `${color} ${start}deg ${end}deg`
    start = end
    return stop
  })
  return `conic-gradient(${stops.join(', ')})`
})
</script>

<style scoped>
.pie-layout {
  display: grid;
  grid-template-columns: minmax(180px, 260px) 1fr;
  gap: 24px;
  align-items: center;
}

.donut {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  margin: 0 auto;
}

.donut-hole {
  width: 118px;
  height: 118px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: #0f172a;
}

.legend {
  display: grid;
  gap: 12px;
}

.legend-item {
  display: grid;
  grid-template-columns: 12px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
}

@media (max-width: 768px) {
  .pie-layout {
    grid-template-columns: 1fr;
  }
}
</style>
