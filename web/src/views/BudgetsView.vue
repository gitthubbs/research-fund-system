<template>
  <AdminBudgetManage v-if="role === 'admin'" />
  <ResearcherBudgetSearch v-else-if="role === 'researcher'" />
</template>

<script setup>
import { computed } from 'vue'
import { getUser } from '@/utils/auth'
import AdminBudgetManage from './admin/BudgetManageView.vue'
import ResearcherBudgetSearch from './researcher/BudgetSearchView.vue'

const user = getUser() || {}
const role = computed(() => user.role)
</script>
