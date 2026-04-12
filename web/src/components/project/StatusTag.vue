<template>
  <el-tag :type="tagType" :size="size" :effect="effect" :round="round">
    {{ status }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
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
  },
  round: {
    type: Boolean,
    default: false
  }
})

const tagType = computed(() => {
    const map = {
      '未提交': 'info',
      '待审核': 'warning',
      '已驳回': 'danger',
      '待编制预算': 'primary',
      '执行中': 'success',
      '已结题': 'info'
    }
  return map[props.status] || 'info'
})
</script>
