<template>
  <div class="page profile-container">
    <!-- 1. 英雄区 -->
    <header class="profile-hero">
      <div class="hero-body">
        <div class="user-avatar-wrapper">
          <div class="avatar-placeholder">
            {{ userInfo.realName?.substring(0, 1) || 'U' }}
          </div>
          <div class="role-badge">{{ userInfo.role === 'admin' ? '管理员' : '科研人员' }}</div>
        </div>
        <div class="hero-info">
          <h1>{{ userInfo.realName || '请设置姓名' }}</h1>
          <p class="username-tag">账号: {{ userInfo.username }}</p>
          <div class="meta-info">
            <span><el-icon><Calendar /></el-icon> 注册于 {{ userInfo.createTime?.split('T')[0] || '--' }}</span>
            <span class="divider">|</span>
            <span><el-icon><OfficeBuilding /></el-icon> {{ userInfo.department || '未关联部门' }}</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 2. 设置主体 -->
    <el-card shadow="never" class="main-settings-card">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 资料修改页 -->
        <el-tab-pane label="个人资料" name="basic">
          <el-form 
            ref="profileFormRef"
            :model="profileForm" 
            :rules="profileRules"
            label-position="top" 
            class="settings-form"
          >
            <el-row :gutter="40">
              <el-col :span="12">
                <el-form-item label="姓名" prop="realName">
                  <el-input v-model="profileForm.realName" placeholder="请填写您的真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="部门" prop="department">
                  <el-input v-model="profileForm.department" placeholder="请填写所属部门" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="40">
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="11位手机号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="工作邮箱地址" />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="form-footer">
              <el-button type="primary" :loading="saving" @click="handleUpdateProfile">
                保存资料修改
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置页 -->
        <el-tab-pane label="账户安全" name="security">
          <div class="security-intro">
            <h3>修改登录密码</h3>
            <p>为了保障您的资金管理安全，建议定期更换高强度密码。</p>
          </div>
          <el-form 
            ref="pwdFormRef"
            :model="pwdForm" 
            :rules="pwdRules"
            label-width="100px" 
            class="security-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位字符" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" :loading="pwdLoading" @click="handleChangePwd">
                更新账户密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, OfficeBuilding } from '@element-plus/icons-vue'
import { profileApi } from '@/api/profileApi'

const activeTab = ref('basic')
const userInfo = ref({})
const saving = ref(false)
const pwdLoading = ref(false)

const profileFormRef = ref(null)
const profileForm = reactive({
  realName: '',
  department: '',
  phone: '',
  email: ''
})

const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 校验规则
const profileRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入11位手机号', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请验证旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function loadProfile() {
  try {
    const res = await profileApi.getMine()
    userInfo.value = res.data || {}
    // 同步到表单
    profileForm.realName = userInfo.value.realName
    profileForm.department = userInfo.value.department
    profileForm.phone = userInfo.value.phone
    profileForm.email = userInfo.value.email
  } catch (e) {
    ElMessage.error('无法提取用户信息')
  }
}

async function handleUpdateProfile() {
  await profileFormRef.value.validate()
  saving.value = true
  try {
    await profileApi.update(profileForm)
    ElMessage.success('个人资料更新成功')
    await loadProfile() // 刷新 Hero 区域显示
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    saving.value = false
  }
}

async function handleChangePwd() {
  await pwdFormRef.value.validate()
  
  ElMessageBox.confirm('修改密码后需要重新登录，是否继续？', '安全提示', {
    confirmButtonText: '确定修改',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    pwdLoading.value = true
    try {
      await profileApi.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
      ElMessage.success('密码修改成功，请重新登录')
      // 实际项目中应在此处清除 token 并跳转登录页
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '密码更新失败，请检查旧密码')
    } finally {
      pwdLoading.value = false
    }
  }).catch(() => {})
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 英雄区设计 */
.profile-hero {
  padding: 40px;
  border-radius: 28px;
  background: 
    radial-gradient(circle at 80% 20%, rgba(99, 102, 241, 0.15), transparent 40%),
    linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #e0f2fe;
}

.hero-body {
  display: flex;
  align-items: center;
  gap: 32px;
}

.user-avatar-wrapper {
  position: relative;
}

.avatar-placeholder {
  width: 96px;
  height: 96px;
  border-radius: 28px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 40px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.25);
}

.role-badge {
  position: absolute;
  bottom: -8px;
  right: -8px;
  padding: 4px 12px;
  background: #fff;
  color: #1e293b;
  font-size: 12px;
  font-weight: 700;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.hero-info h1 {
  margin: 0;
  font-size: 30px;
  color: #1e293b;
  letter-spacing: -0.5px;
}

.username-tag {
  color: #64748b;
  margin: 6px 0 12px;
  font-family: monospace;
  font-size: 14px;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #64748b;
  font-size: 14px;
}

.divider { color: #cbd5e1; }

/* 主卡片设置 */
.main-settings-card {
  border-radius: 24px;
  border: 1px solid #f1f5f9;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  height: 54px;
  font-weight: 600;
}

.settings-form {
  padding: 20px 0;
  max-width: 800px;
}

.form-footer {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

/* 安全设置部分 */
.security-intro {
  padding: 10px 0 24px;
}

.security-intro h3 { margin: 0 0 8px; color: #1e293b; }
.security-intro p { color: #64748b; font-size: 14px; }

.security-form {
  max-width: 500px;
}
</style>
