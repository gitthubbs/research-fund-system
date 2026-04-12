<template>
  <AdminExpenditureMonitor v-if="role === 'admin'" />
  <ResearcherExpenditureRecord v-else-if="role === 'researcher'" />
</template>

<script setup>
import { computed } from 'vue'
import { getUser } from '@/utils/auth'
import AdminExpenditureMonitor from './admin/ExpenditureMonitorView.vue'
import ResearcherExpenditureRecord from './researcher/ExpenditureRecordView.vue'

const user = getUser() || {}
const role = computed(() => user.role)
</script>
