<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>预算调剂审核</h2>
        <p class="muted">审批科研人员提交的跨科目预算划转申请</p>
      </div>
      <el-tag type="info" size="large">待处理申请: {{ pendingCount }}</el-tag>
    </div>

    <el-card shadow="never">
      <el-table :data="rows" v-loading="loading" empty-text="当前没有待处理的调剂申请">
        <el-table-column prop="projectName" label="项目名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="调剂路径" min-width="240">
          <template #default="{ row }">
            <div class="adj-path">
              <el-tag type="danger" size="small">{{ row.fromCategoryName }}</el-tag>
              <el-icon class="arrow"><Right /></el-icon>
              <el-tag type="success" size="small">{{ row.toCategoryName }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="调剂金额" width="140">
          <template #default="{ row }">
            <span class="amount-text">{{ formatCurrency(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="reason" label="调剂理由" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="success" 
              size="small" 
              @click="handleAudit(row, 1)"
            >通过</el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleAudit(row, 2)"
            >驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right } from '@element-plus/icons-vue'
import { adjustmentApi } from '@/api/adjustmentApi'
import { formatCurrency } from '@/utils/format'

const rows = ref([])
const loading = ref(false)
const pendingCount = computed(() => rows.value.length)

async function loadData() {
  loading.value = true
  try {
    const response = await adjustmentApi.getPending()
    rows.value = response.data || []
  } catch (error) {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

async function handleAudit(row, status) {
  const actionText = status === 1 ? '通过' : '驳回'
  let auditRemark = ''
  
  try {
    if (status === 2) {
      const result = await ElMessageBox.prompt('请输入驳回理由：', '调剂驳回', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '理由不能为空',
        type: 'error'
      })
      auditRemark = result.value
    } else {
      await ElMessageBox.confirm(
        `确定批准该笔调剂申请吗？从 ${row.fromCategoryName} 调拨 ${formatCurrency(row.amount)} 至 ${row.toCategoryName}`,
        '调剂审批确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }
    
    await adjustmentApi.audit({ 
      id: row.id, 
      status,
      auditRemark: auditRemark 
    })
    ElMessage.success(`调剂申请已${actionText}`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header h2 { font-size: 24px; font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.muted { color: #64748b; font-size: 14px; }
.adj-path { display: flex; align-items: center; gap: 8px; }
.arrow { color: #94a3b8; }
.amount-text { font-weight: 600; color: #1d4ed8; }
</style>
