<template>
  <div class="login-page">
    <div class="panel intro">
      <p class="eyebrow">Research Fund System</p>
      <h1>科研经费管理系统</h1>
      <p class="copy">支持管理员与科研人员双角色登录、预算预警和项目绩效跟踪。</p>
      <div class="accounts">
        <div class="account">
          <strong>管理员</strong>
          <span>`admin / 123456`</span>
        </div>
        <div class="account">
          <strong>科研人员</strong>
          <span>`zhangsan / 123456`</span>
        </div>
      </div>
    </div>

    <el-card class="panel form-panel" shadow="hover">
      <template #header>
        <div>
          <h2>登录</h2>
          <p class="muted">输入账号后进入对应角色工作台。</p>
        </div>
      </template>
      <el-alert v-if="notice" type="warning" :title="notice" show-icon class="alert" />
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="login">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="login">登录系统</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { clearReloginNotice, saveAuthSession, shouldShowReloginNotice } from '@/utils/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const notice = ref(shouldShowReloginNotice() ? '角色已变更，请重新登录进行验证。' : '')

const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function login() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const response = await request.post('/auth/login', form)
    saveAuthSession(response.data)
    clearReloginNotice()
    ElMessage.success(`${response.data.user.name}，欢迎回来`)
    router.push(response.data.role === 'admin' ? '/projects' : '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  padding: 32px;
  gap: 24px;
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.25), transparent 28%),
    linear-gradient(135deg, #e8f1ff, #f8fbff 55%, #eef2ff);
}

.panel {
  border-radius: 28px;
}

.intro {
  padding: 48px;
  background: linear-gradient(145deg, #0f172a, #1e3a8a);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  opacity: 0.72;
  margin-bottom: 12px;
}

.copy {
  max-width: 420px;
  margin-top: 16px;
  color: rgba(255, 255, 255, 0.8);
}

.accounts {
  display: grid;
  gap: 12px;
  margin-top: 32px;
}

.account {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
}

.account span {
  display: block;
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.78);
}

.form-panel {
  align-self: center;
  padding: 8px;
}

.muted {
  color: #64748b;
}

.alert {
  margin-bottom: 16px;
}

.submit {
  width: 100%;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
  }
}
</style>
