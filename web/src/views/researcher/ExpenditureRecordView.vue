<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>报销记录查询</h2>
        <p class="muted">查询您负责项目的报销历史明细及当前状态。</p>
      </div>
      <!-- ★ 修改：点击后跳转到专业的报销录入页 -->
      <el-button type="primary" @click="gotoAdd">去报销录入</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="选择项目">
          <el-select v-model="filters.projectId" style="width: 260px" @change="loadExpenditures">
            <el-option v-for="project in projects" :key="project.id" :label="project.projectName" :value="project.id">
              <span style="float: left">{{ project.projectName }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ project.status }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="loadExpenditures">刷新列表</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" empty-text="当前项目暂无支出记录">
        <el-table-column prop="categoryName" label="支出类别" width="140" />
        <el-table-column label="支出金额" width="140">
          <template #default="{ row }">{{ formatCurrency(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="expenditureDate" label="支出日期" width="120" />
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="审核意见" min-width="200">
          <template #default="{ row }">
            <span v-if="row.status === 2" style="color: var(--el-color-danger)">
              {{ row.auditRemark || '无理由' }}
            </span>
            <span v-else-if="row.status === 1" style="color: var(--el-color-info)">
              {{ row.auditRemark || '-' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 0" 
              link 
              type="primary" 
              @click="handleDelete(row, 'revoke')"
            >撤销</el-button>
            <el-button 
              v-if="row.status === 2" 
              link 
              type="danger" 
              @click="handleDelete(row, 'delete')"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { expenditureApi } from '@/api/expenditureApi'
import { projectApi } from '@/api/projectApi'
import { getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'

const router = useRouter()
const projects = ref([])
const rows = ref([])

const currentUser = getUser() || {}

const filters = reactive({
  projectId: undefined
})

async function loadData() {
  const projectRes = await projectApi.getList()
  // 只过滤由于该用户负责的项目
  projects.value = projectRes.data.filter(p => p.principalId === currentUser.id)
  
  if (projects.value.length) {
    filters.projectId = projects.value[0].id
    await loadExpenditures()
  }
}

async function loadExpenditures() {
  if (!filters.projectId) return
  const response = await expenditureApi.getByProject(filters.projectId)
  rows.value = response.data || []
}

function gotoAdd() {
  router.push('/expenditures/add')
}

// ★ 新增：撤销/删除逻辑
async function handleDelete(row, type) {
  const actionText = type === 'revoke' ? '撤销' : '删除'
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}该报销申请吗？${type === 'revoke' ? '撤销后可重新提交。' : '删除后数据将无法恢复。'}`,
      '操作确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    await expenditureApi.delete(row.id)
    ElMessage.success(`${actionText}成功`)
    await loadExpenditures()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const statusLabel = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #64748b; font-size: 14px; }
</style>
