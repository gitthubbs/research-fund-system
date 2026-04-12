<template>
  <el-tag :type="tagType" :size="size" :effect="effect">
    {{ performance }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  performance: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'small'
  },
  effect: {
    type: String,
    default: 'light'
  }
})

const tagType = computed(() => {
  const map = {
    '优': 'success',
    '良': 'primary',
    '中': 'warning',
    '差': 'danger'
  }
  return map[props.performance] || 'info'
})
</script>
