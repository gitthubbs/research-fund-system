<template>
  <div class="chart-shell">
    <svg v-if="points.length" viewBox="0 0 100 60" preserveAspectRatio="none" class="chart-svg">
      <polyline
        fill="none"
        stroke="#1f6feb"
        stroke-width="2"
        :points="points.join(' ')"
      />
      <line
        v-for="marker in markers"
        :key="marker.x"
        :x1="marker.x"
        y1="0"
        :x2="marker.x"
        y2="60"
        class="grid-line"
      />
      <circle
        v-for="marker in markers"
        :key="`${marker.x}-${marker.value}`"
        :cx="marker.x"
        :cy="marker.y"
        r="1.8"
        fill="#1f6feb"
      />
    </svg>
    <div class="chart-footer">
      <span v-for="item in data" :key="item.month">{{ item.month }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const markers = computed(() => {
  if (!props.data.length) return []
  const max = Math.max(...props.data.map((item) => item.amount), 1)
  return props.data.map((item, index) => {
    const x = props.data.length === 1 ? 50 : (index / (props.data.length - 1)) * 100
    const y = 56 - (item.amount / max) * 48
    return { x, y, value: item.amount }
  })
})

const points = computed(() => markers.value.map((item) => `${item.x},${item.y}`))
</script>

<style scoped>
.chart-shell {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-svg {
  width: 100%;
  height: 280px;
  background:
    linear-gradient(180deg, rgba(31, 111, 235, 0.08), rgba(31, 111, 235, 0)),
    #f8fbff;
  border-radius: 16px;
}

.grid-line {
  stroke: rgba(15, 23, 42, 0.08);
  stroke-width: 0.4;
}

.chart-footer {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}
</style>
