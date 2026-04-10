<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div>
          <h2>个人中心</h2>
          <p class="muted">支持修改个人资料和重置密码。</p>
        </div>
      </template>
      <el-form :model="form" label-width="96px" class="profile-form">
        <el-form-item label="账号"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi } from '@/api/profileApi'

const form = reactive({
  username: '',
  name: '',
  department: '',
  phone: '',
  email: '',
  password: ''
})

async function loadProfile() {
  const response = await profileApi.getMine()
  Object.assign(form, response.data, { password: '' })
}

async function save() {
  await profileApi.update(form)
  ElMessage.success('个人信息已保存')
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-form {
  max-width: 620px;
}

.muted {
  color: #64748b;
}
</style>
