<template>
  <div class="page">
    <div class="summary">
      <div>
        <p class="eyebrow">项目详情</p>
        <h2>{{ project.projectName }}</h2>
        <p class="muted">{{ project.projectCode }} · 负责人 {{ project.principalName }}</p>
      </div>
      <el-tag :type="tagType">{{ project.performance }}</el-tag>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>项目里程碑</template>
          <el-timeline>
            <el-timeline-item
              v-for="item in project.milestones"
              :key="`${item.stage}-${item.date}`"
              :timestamp="item.date"
              placement="top"
            >
              <strong>{{ item.stage }}</strong>
              <p>{{ item.content }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header>绩效评级</template>
          <div class="rate-card">
            <el-rate :model-value="rateValue" disabled show-score />
            <el-tag :type="tagType" size="large">{{ project.performance }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>预算执行概览</template>
      <el-table :data="project.budgets" empty-text="暂无数据">
        <el-table-column prop="categoryName" label="预算科目" />
        <el-table-column label="预算金额">
          <template #default="{ row }">{{ formatCurrency(row.budgetAmount) }}</template>
        </el-table-column>
        <el-table-column label="已执行">
          <template #default="{ row }">{{ formatCurrency(row.spentAmount) }}</template>
        </el-table-column>
        <el-table-column label="执行率">
          <template #default="{ row }">{{ formatPercent(row.executionRate) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { projectApi } from '@/api/projectApi'
import { formatCurrency, formatPercent } from '@/utils/format'

const route = useRoute()
const project = reactive({
  projectName: '',
  projectCode: '',
  principalName: '',
  performance: '中',
  milestones: [],
  budgets: []
})

const rateMap = { 优: 5, 良: 4, 中: 3, 差: 2 }
const typeMap = { 优: 'success', 良: 'primary', 中: 'warning', 差: 'danger' }
const rateValue = computed(() => rateMap[project.performance] || 3)
const tagType = computed(() => typeMap[project.performance] || 'info')

async function loadDetail() {
  const response = await projectApi.getById(route.params.id)
  Object.assign(project, response.data)
}

onMounted(loadDetail)
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.eyebrow {
  color: #1d4ed8;
  font-weight: 600;
}

.muted {
  color: #64748b;
}

.rate-card {
  min-height: 180px;
  display: grid;
  place-items: center;
  gap: 12px;
}
</style>
