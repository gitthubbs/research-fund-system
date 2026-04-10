<template>
  <div class="header">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item to="/dashboard">首页</el-breadcrumb-item>
      <el-breadcrumb-item v-for="item in crumbs" :key="item.path">{{ item.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="actions">
      <el-avatar>{{ (user?.name || 'U').slice(-1) }}</el-avatar>
      <div class="meta">
        <strong>{{ user?.name }}</strong>
        <span>{{ user?.department }}</span>
      </div>
      <el-tag :type="roleType(user?.role)">{{ roleLabel(user?.role) }}</el-tag>
      <el-button link type="danger" @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { clearAuthSession, getUser } from '@/utils/auth'
import { roleLabel, roleType } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

const crumbs = computed(() =>
  route.matched.filter((item) => item.meta?.title && item.path !== '/')
)

async function logout() {
  await ElMessageBox.confirm('确认退出当前账号？', '退出登录', { type: 'warning' })
  clearAuthSession()
  router.push('/login')
}
</script>

<style scoped>
.header {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta {
  display: flex;
  flex-direction: column;
}

.meta span {
  color: #64748b;
  font-size: 12px;
}
</style>
