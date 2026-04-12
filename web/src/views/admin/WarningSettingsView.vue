<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <div>
            <h2>预警配置</h2>
            <p>仅管理员可见，用于设置全局预算执行率预警阈值。</p>
          </div>
        </div>
      </template>

      <el-form label-width="160px" class="settings-form">
        <el-form-item label="全局预警阈值">
          <el-input-number v-model="threshold" :min="1" :max="100" />
          <span class="hint">当预算执行率超过该阈值时，高亮显示预警。</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { settingsApi } from '@/api/settingsApi'

const threshold = ref(90)

async function loadThreshold() {
  const response = await settingsApi.getWarningThreshold()
  threshold.value = response.data.value
}

async function save() {
  await settingsApi.updateWarningThreshold(threshold.value)
  ElMessage.success('预警阈值已更新')
}

onMounted(loadThreshold)
</script>

<style scoped>
.header p,
.hint {
  color: #64748b;
}

.settings-form {
  max-width: 680px;
}

.hint {
  margin-left: 12px;
}
</style>
