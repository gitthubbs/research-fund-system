<template>
  <div class="page">
    <div class="header">
      <el-button @click="$router.back()" link :icon="ArrowLeft">返回列表</el-button>
      <h2>新增科研项目</h2>
      <p class="muted">填写项目基本信息，保存后可进行申请提交。</p>
    </div>

    <el-card class="form-card" shadow="hover">
      <ProjectForm 
        ref="formRef"
        v-model="form" 
        :is-admin="false"
        label-position="top"
      />
      
      <div class="form-actions">
        <el-button @click="$router.push('/projects')">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSave" :icon="Check">保存为草稿</el-button>
      </div>
    </el-card>

    <div class="notice">
      <el-alert title="说明" type="info" show-icon :closable="false">
        项目保存后默认为“草稿”状态，您可以在项目列表页找到该项目并点击“提交申请”发起审核流程。
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { projectApi } from '@/api/projectApi'
import ProjectForm from '@/components/project/ProjectForm.vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  projectName: '',
  projectCode: '',
  startDate: '',
  endDate: '',
  totalBudget: 0
})

async function handleSave() {
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    await projectApi.create(form)
    ElMessage.success('项目已保存为草稿')
    router.push('/projects')
  } catch (err) {
    console.error('保存失败:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { max-width: 800px; margin: 0 auto; }
.header { margin-bottom: 24px; }
.header h2 { margin: 12px 0 4px; font-size: 24px; color: #1e293b; }
.muted { color: #64748b; font-size: 14px; }
.form-card { border-radius: 12px; border: 1px solid rgba(148, 163, 184, 0.2); }
.form-actions { margin-top: 32px; padding-top: 24px; border-top: 1px solid #f1f5f9; display: flex; justify-content: flex-end; gap: 12px; }
.notice { margin-top: 24px; }
</style>
