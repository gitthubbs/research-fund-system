<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>项目审核</h2>
        <p class="muted">处理科研人员提交的项目立项申请。</p>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="auditProjects" empty-text="暂无待审核项">
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectCode" label="项目编号" width="160" />
        <el-table-column prop="principalName" label="负责人" width="120" />
        <el-table-column label="总预算" width="150">
          <template #default="{ row }">{{ formatCurrency(row.totalBudget) }}</template>
        </el-table-column>
        <el-table-column prop="updateTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="visible" title="项目立项审核" width="640px" destroy-on-close>
      <div v-if="currentProject" class="audit-content">
        <el-descriptions title="申请信息详情" :column="2" border size="small">
          <el-descriptions-item label="项目名称" :span="2">{{ currentProject.projectName }}</el-descriptions-item>
          <el-descriptions-item label="项目编号">{{ currentProject.projectCode }}</el-descriptions-item>
          <el-descriptions-item label="责任人">{{ currentProject.principalName }}</el-descriptions-item>
          <el-descriptions-item label="预算总额">
            <span class="price">{{ formatCurrency(currentProject.totalBudget) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="起止日期">
            {{ currentProject.startDate }} ~ {{ currentProject.endDate }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">审核处理</el-divider>

        <el-form :model="auditForm" label-position="top">
          <el-form-item label="审核结果" required>
            <el-radio-group v-model="auditForm.status">
              <el-radio :label="2" border>通过</el-radio>
              <el-radio :label="3" border>驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见" :required="auditForm.status === 3">
            <el-input 
              v-model="auditForm.auditRemark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入处理意见（驳回时必填）" 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleAudit" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { projectApi } from '@/api/projectApi'
import { formatCurrency } from '@/utils/format'

const projects = ref([])
const visible = ref(false)
const submitting = ref(false)
const currentProject = ref(null)

const auditForm = reactive({
  id: null,
  status: 2,
  auditRemark: ''
})

const auditProjects = computed(() => 
  projects.value.filter(p => p.status === '待审核')
)

async function loadData() {
  const res = await projectApi.getList()
  projects.value = res.data
}

function openAudit(row) {
  currentProject.value = row
  auditForm.id = row.id
  auditForm.status = 2
  auditForm.auditRemark = ''
  visible.value = true
}

async function handleAudit() {
  if (auditForm.status === 3 && !auditForm.auditRemark.trim()) {
    ElMessage.warning('驳回时必须填写审核意见')
    return
  }

  submitting.value = true
  try {
    await projectApi.audit(auditForm)
    ElMessage.success('审批处理完成')
    visible.value = false
    await loadData()
  } catch (err) {
    ElMessage.error('处理失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #64748b; font-size: 14px; }
.price { color: #e11d48; font-weight: 600; font-size: 15px; }
.audit-content :deep(.el-descriptions__title) { font-size: 15px; color: #334155; }
.audit-content :deep(.el-divider__text) { font-weight: 600; color: #64748b; }
</style>
