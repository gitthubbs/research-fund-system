<template>
  <div class="page">
    <div>
      <h2>系统日志</h2>
      <p class="muted">只读操作记录，用于审计和排查。</p>
    </div>
    <el-card shadow="never">
      <el-table :data="logs" empty-text="暂无数据">
        <el-table-column prop="time" label="操作时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="140" />
        <el-table-column prop="module" label="模块" width="140" />
        <el-table-column prop="content" label="操作内容" min-width="320" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { logApi } from '@/api/logApi'

const logs = ref([])

async function loadLogs() {
  const response = await logApi.getList()
  logs.value = response.data
}

onMounted(loadLogs)
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.muted {
  color: #64748b;
}
</style>
