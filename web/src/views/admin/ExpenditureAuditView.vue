<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>报销审核</h2>
        <p class="muted">处理来自科研人员的经费报销申请</p>
      </div>
      <el-tag type="info" size="large">待处理申请: {{ pendingCount }}</el-tag>
    </div>

    <!-- ★ 新增筛选区块 -->
    <div class="filter-row">
      <el-form :inline="true" size="default">
        <el-form-item label="按负责人筛选">
          <el-select v-model="filters.applicant" placeholder="请选择负责人" clearable style="width: 180px">
            <el-option v-for="name in applicantOptions" :key="name" :label="name" :value="name" />
          </el-select>
        </el-form-item>
        <el-form-item label="按项目筛选">
          <el-select v-model="filters.project" placeholder="请选择项目" clearable style="width: 240px">
            <el-option v-for="name in projectOptions" :key="name" :label="name" :value="name" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table :data="filteredRows" v-loading="loading" empty-text="当前没有匹配的待处理申请">
        <el-table-column prop="projectName" label="所属项目" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="支出科目" width="130" />
        <el-table-column label="报销金额" width="140">
          <template #default="{ row }">
            <span class="amount-text">{{ formatCurrency(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="expenditureDate" label="支出日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { expenditureApi } from '@/api/expenditureApi'
import { formatCurrency } from '@/utils/format'

const rows = ref([])
const loading = ref(false)
const pendingCount = computed(() => filteredRows.value.length)

const filters = reactive({
  applicant: '',
  project: ''
})

const applicantOptions = computed(() => {
  const names = rows.value.map(r => r.applicantName).filter(n => n != null)
  return [...new Set(names)]
})

const projectOptions = computed(() => {
  const names = rows.value.map(r => r.projectName).filter(n => n != null)
  return [...new Set(names)]
})

const filteredRows = computed(() => {
  return rows.value.filter(r => {
    const matchApplicant = !filters.applicant || r.applicantName === filters.applicant
    const matchProject = !filters.project || r.projectName === filters.project
    return matchApplicant && matchProject
  })
})

function resetFilters() {
  filters.applicant = ''
  filters.project = ''
}

async function loadData() {
  loading.value = true
  try {
    const response = await expenditureApi.getPending()
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
      // 驳回时强制要求填写理由
      const result = await ElMessageBox.prompt('请输入驳回理由：', '驳回确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '理由不能为空',
        type: 'error'
      })
      auditRemark = result.value
    } else {
      // 通过时简单确认
      await ElMessageBox.confirm(
        `确定要通过该笔报销申请吗？金额: ${formatCurrency(row.amount)}`,
        '审批确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }
    
    await expenditureApi.audit({ 
      id: row.id, 
      status,
      auditRemark: auditRemark 
    })
    ElMessage.success(`审批已${actionText}`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(loadData)
</script>

<style scoped>
.page {
  display: grid;
  gap: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-row {
  background-color: #f8fafc;
  padding: 16px 20px 0;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  margin-bottom: 16px;
}

.filter-row :deep(.el-form-item) {
  margin-bottom: 16px;
}

.header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.muted {
  color: #64748b;
  font-size: 14px;
}

.amount-text {
  font-weight: 600;
  color: #1d4ed8;
}

:deep(.el-table__header) {
  background-color: #f8fafc;
}

:deep(.el-table__cell) {
  padding: 12px 0;
}
</style>
