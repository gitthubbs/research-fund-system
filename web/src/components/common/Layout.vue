<template>
  <el-container class="layout-shell" :class="`role-${user?.role || 'researcher'}`">
    <el-aside class="sidebar-wrap" :width="isMobile ? '88px' : isCollapse ? '88px' : '260px'">
      <Sidebar :is-collapse="isMobile || isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <Header />
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import Header from '@/components/common/Header.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import { getUser } from '@/utils/auth'

const isCollapse = ref(false)
const width = ref(window.innerWidth)
const user = computed(() => getUser())
const isMobile = computed(() => width.value < 960)

function onResize() {
  width.value = window.innerWidth
}

onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background: #f0f2f5;
}

.sidebar-wrap {
  transition: width 0.25s ease;
}

.layout-header {
  height: 72px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.layout-main {
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.06), transparent 25%),
    #f0f2f5;
}

.role-admin .layout-main {
  background:
    radial-gradient(circle at top right, rgba(0, 21, 41, 0.08), transparent 25%),
    #f0f2f5;
}
</style>
