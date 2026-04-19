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

    <!-- ★ 新增：极简汇总提醒栏 (Compact Alert Bar) -->
    <div v-if="alertList.length > 0" class="compact-alert-bar" @click="drawerVisible = true">
      <div class="alert-summary">
        <el-icon class="main-alert-icon"><Warning /></el-icon>
        <span class="summary-text">
          报销提醒：当前有 
          <b v-if="rejectedCount" class="danger-text">{{ rejectedCount }} 笔驳回</b>
          <span v-if="rejectedCount && pendingCount">，</span>
          <b v-if="pendingCount" class="warning-text">{{ pendingCount }} 笔审核中</b>
          记录需要关注。
        </span>
      </div>
      <div class="view-detail">
        查看详情 <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- ★ 新增：详情抽屉 (Detail Drawer) -->
    <el-drawer
      v-model="drawerVisible"
      title="待办/预警报销详情"
      direction="rtl"
      size="420px"
    >
      <div class="drawer-content">
        <div v-for="item in alertList" :key="item.id" :class="['mini-alert-card', item.status === 2 ? 'is-danger' : 'is-warning']">
          <div class="card-header">
            <el-tag :type="item.status === 2 ? 'danger' : 'warning'" size="small">
              {{ item.status === 2 ? '被驳回' : '审核中' }}
            </el-tag>
            <span class="card-amount">{{ formatCurrency(item.amount) }}</span>
          </div>
          <div class="card-body">
            <div class="proj-info">{{ item.projectName }}</div>
            <div class="cate-info">{{ item.categoryName }}</div>
            <div v-if="item.status === 2" class="reason-box">驳回理由：{{ item.auditRemark || '未填写理由' }}</div>
            <div v-else class="time-info">申请时间：{{ item.createTime || '-' }}</div>
          </div>
          <div class="card-footer" v-if="item.status === 2">
            <el-button type="danger" size="small" plain @click="markRead(item.id)">标记已读</el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="选择项目">
          <el-select v-model="filters.projectId" style="width: 320px" @change="loadExpenditures">
            <el-option v-for="project in projects" :key="project.id" :label="project.projectName" :value="project.id">
              <span style="float: left">
                {{ project.projectName }}
                <el-icon v-if="hasAlert(project.id)" class="warning-icon" title="该项目有待办或被驳回报销"><Warning /></el-icon>
              </span>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { expenditureApi } from '@/api/expenditureApi'
import { projectApi } from '@/api/projectApi'
import { getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'
import { Warning, InfoFilled, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const projects = ref([])
const rows = ref([])
const alertList = ref([]) // ★ 新增：异常/待办列表
const drawerVisible = ref(false) // ★ 新增：控制详情抽屉

const currentUser = getUser() || {}

const filters = reactive({
  projectId: undefined
})

const rejectedCount = computed(() => alertList.value.filter(a => a.status === 2).length)
const pendingCount = computed(() => alertList.value.filter(a => a.status === 0).length)

async function loadData() {
  const [projectRes, alertRes] = await Promise.all([
    projectApi.getList(),
    expenditureApi.getAlerts()
  ])
  
  // 只过滤由于该用户负责的项目
  projects.value = projectRes.data.filter(p => p.principalId === currentUser.id)
  alertList.value = alertRes.data || []
  
  if (projects.value.length) {
    // 优先从 URL 参数中获取项目 ID
    const queryPid = Number(route.query.projectId)
    if (queryPid && projects.value.some(p => p.id === queryPid)) {
      filters.projectId = queryPid
    } else {
      filters.projectId = projects.value[0].id
    }
    await loadExpenditures()
  }
}

function hasAlert(projectId) {
  return alertList.value.some(a => a.projectId === projectId)
}

async function markRead(id) {
  await expenditureApi.read(id)
  ElMessage.success('状态已更新')
  // 局部从提醒列表中移除，无需全页刷新
  alertList.value = alertList.value.filter(a => a.id !== id)
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

.warning-icon {
  margin-left: 6px;
  color: #ef4444;
  vertical-align: middle;
}

/* ★ 修改：极简汇总列表样式 */
.compact-alert-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.compact-alert-bar:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.alert-summary {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main-alert-icon {
  color: #ef4444;
  font-size: 18px;
}

.summary-text {
  font-size: 14px;
  color: #475569;
}

.danger-text { color: #ef4444; }
.warning-text { color: #f59e0b; }

.view-detail {
  font-size: 13px;
  color: #3b82f6;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ★ 修改：抽屉内部紧凑卡片 */
.drawer-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px;
}

.mini-alert-card {
  padding: 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  transition: transform 0.2s;
}

.mini-alert-card.is-danger { border-left: 4px solid #ef4444; }
.mini-alert-card.is-warning { border-left: 4px solid #f59e0b; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-amount {
  font-weight: 700;
  color: #1e293b;
}

.card-body {
  font-size: 14px;
  line-height: 1.6;
}

.proj-info { font-weight: 600; color: #334155; }
.cate-info { color: #64748b; margin-top: 2px; }

.reason-box {
  background: #fef2f2;
  color: #b91c1c;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  margin-top: 10px;
}

.time-info {
  color: #94a3b8;
  font-size: 12px;
  margin-top: 8px;
}

.card-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.warning-icon {
  margin-left: 6px;
  color: #ef4444;
  vertical-align: middle;
}
</style>
