<template>
  <el-card shadow="never" class="chart-card" v-loading="loading">
    <template #header>
      <div class="card-head">
        <span>{{ title }}</span>
        <small v-if="subtitle">{{ subtitle }}</small>
      </div>
    </template>
    <slot />
  </el-card>
</template>

<script setup>
// ★ 新增
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.chart-card {
  border-radius: 20px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-head small {
  color: #64748b;
}
</style>
