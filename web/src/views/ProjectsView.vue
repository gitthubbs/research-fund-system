<template>
  <AdminProjectManage v-if="role === 'admin'" />
  <ResearcherProjectList v-else-if="role === 'researcher'" />
  <div v-else class="error">无权访问该角色页面</div>
</template>

<script setup>
import { computed } from 'vue'
import { getUser } from '@/utils/auth'
import AdminProjectManage from './admin/ProjectManageView.vue'
import ResearcherProjectList from './researcher/ProjectListView.vue'

const user = getUser() || {}
const role = computed(() => user.role)
</script>

<style scoped>
.error {
  padding: 40px;
  text-align: center;
  color: #ef4444;
}
</style>
