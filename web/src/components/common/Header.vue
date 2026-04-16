<template>
  <div class="header">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item to="/dashboard">首页</el-breadcrumb-item>
      <el-breadcrumb-item
        v-for="item in crumbs"
        :key="item.path"
        :to="item.path"
      >
        {{ item.meta.title }}
      </el-breadcrumb-item> <!-- ★ 修改：添加点击跳转 -->
    </el-breadcrumb>

    <div class="actions">
      <el-button
        circle
        :icon="Refresh"
        class="refresh-btn"
        :class="{ 'is-spinning': isRefreshing }"
        @click="handleRefresh"
        title="刷新当前路由"
      /> <!-- ★ 修改：添加动画 class -->
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
import { computed, inject, ref } from 'vue' // ★ 修改
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { clearAuthSession, getUser } from '@/utils/auth'
import { roleLabel, roleType } from '@/utils/format'
import { Refresh } from '@element-plus/icons-vue' // ★ 新增

const reload = inject('reload') // ★ 新增
const isRefreshing = ref(false) // ★ 新增

const handleRefresh = () => { // ★ 新增
  isRefreshing.value = true
  reload()
  setTimeout(() => {
    isRefreshing.value = false
  }, 600)
}

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

// ★ 修改 面包屑层级计算逻辑
const crumbs = computed(() => {
  const list = []
  const matched = route.matched.filter((item) => item.meta?.title && item.path !== '/')

  if (matched.length > 0) {
    const leaf = matched[matched.length - 1]
    let parentName = leaf.meta?.parent

    const parents = []
    while (parentName) {
      const parentRoute = router.resolve({ name: parentName })
      if (parentRoute && parentRoute.meta?.title) {
        // 防止循环或重复添加已有的匹配项
        if (parents.some((p) => p.name === parentName) || matched.some((m) => m.name === parentName)) break
        parents.unshift(parentRoute)
        parentName = parentRoute.meta?.parent
      } else {
        break
      }
    }
    list.push(...parents)
  }

  list.push(...matched)
  return list
})

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

/* ★ 新增 刷新按钮样式 */
.refresh-btn {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: #64748b;
  margin-right: 8px;
}

.refresh-btn:hover {
  color: #2563eb;
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.05);
}

.refresh-btn:active {
  scale: 0.9;
}

/* ★ 修改：点击时的旋转动画 */
.is-spinning :deep(.el-icon) {
  animation: spin 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
