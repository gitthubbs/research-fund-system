<template>
  <div class="sidebar" :class="themeClass">
    <div class="brand">
      <div class="brand-mark">RF</div>
      <div v-if="!isCollapse">
        <strong>Research Fund</strong>
        <p>{{ roleLabel(user.role) }}</p>
      </div>
      <el-button class="collapse-btn" text @click="$emit('toggle-collapse')">
        <el-icon><Fold /></el-icon>
      </el-button>
    </div>

    <el-menu
      :default-active="$route.path"
      :collapse="isCollapse"
      :background-color="menuColors.background"
      :text-color="menuColors.text"
      :active-text-color="menuColors.active"
      router
      class="menu"
    >
      <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
        <el-icon><component :is="item.icon" /></el-icon>
        <template #title>{{ item.title }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  DataBoard,
  Document,
  FolderOpened,
  Fold,
  Histogram,
  Money,
  User,
  UserFilled,
  Warning,
  Grid
} from '@element-plus/icons-vue'
import { getUser } from '@/utils/auth'
import { roleLabel } from '@/utils/format'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle-collapse'])

const iconMap = {
  DataBoard,
  FolderOpened,
  Money,
  Histogram,
  UserFilled,
  Document,
  User,
  Warning,
  Grid
}

const user = computed(() => getUser() || { role: 'researcher' })

const menus = computed(() => {
  if (user.value.role === 'admin') {
    return [
      { path: '/projects', title: '项目管理', icon: iconMap.FolderOpened },
      { path: '/projects/audit', title: '项目审核', icon: iconMap.Document },
      { path: '/categories', title: '科目维护', icon: iconMap.Grid },
      { path: '/budgets', title: '预算管理', icon: iconMap.Money },
      { path: '/expenditures', title: '支出监控', icon: iconMap.Histogram },
      { path: '/users', title: '用户权限', icon: iconMap.UserFilled },
      { path: '/logs', title: '系统日志', icon: iconMap.Document },
      { path: '/warning-settings', title: '预警配置', icon: iconMap.Warning },
      { path: '/profile', title: '个人设置', icon: iconMap.User }
    ]
  }
  return [
    { path: '/dashboard', title: '我的看板', icon: iconMap.DataBoard },
    { path: '/projects/add', title: '新增项目', icon: iconMap.Document },
    { path: '/projects', title: '项目查询', icon: iconMap.FolderOpened },
    { path: '/budgets', title: '预算编制', icon: iconMap.Money },
    { path: '/profile', title: '个人中心', icon: iconMap.User }
  ]
})

const menuColors = computed(() =>
  user.value.role === 'admin'
    ? { background: '#001529', text: 'rgba(255,255,255,0.8)', active: '#ffffff' }
    : { background: '#ffffff', text: '#334155', active: '#1d4ed8' }
)

const themeClass = computed(() => `theme-${user.value.role}`)
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(148, 163, 184, 0.16);
}

.theme-admin {
  background: #001529;
  color: #fff;
}

.theme-researcher {
  background: #fff;
  color: #0f172a;
}

.brand {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 18px 16px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #2563eb, #0f172a);
  color: #fff;
  font-weight: 700;
}

.brand p {
  opacity: 0.7;
  font-size: 12px;
}

.collapse-btn {
  color: inherit;
}

.menu {
  border-right: none;
  flex: 1;
}

.theme-researcher :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.12), rgba(37, 99, 235, 0.02));
}

.theme-admin :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.12);
}
</style>
