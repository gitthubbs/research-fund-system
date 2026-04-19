<template>
  <div class="page">
      <div>
        <h2>项目审核</h2>
        <p class="muted">处理科研人员提交的项目立项申请。</p>
      </div>
      <div class="filter-area">
        <span class="filter-label">按负责人筛选:</span>
        <el-select v-model="filterPrincipal" placeholder="全部负责人" clearable style="width: 200px">
          <el-option v-for="name in principalOptions" :key="name" :label="name" :value="name" />
        </el-select>
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
        <el-table-column prop="status" label="审核环节" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === '待结题验收' ? 'warning' : 'primary'">{{ row.status }}</el-tag>
          </template>
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
    <el-dialog v-model="visible" :title="currentProject?.status === '待结题验收' ? '项目结题验收审核' : '项目立项审核'" width="640px" destroy-on-close>
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
            <!-- 针对立项审核 -->
            <el-radio-group v-if="currentProject.status === '待审核'" v-model="auditForm.status">
              <el-radio :label="2" border>予以立项 (通过)</el-radio>
              <el-radio :label="3" border>不予立项 (驳回)</el-radio>
            </el-radio-group>
            <!-- 针对结题验收审核 -->
            <el-radio-group v-if="currentProject.status === '待结题验收'" v-model="auditForm.status">
              <el-radio :label="5" border>准予结题归档 (通过)</el-radio>
              <el-radio :label="4" border>退回重新整改 (驳回)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见" :required="auditForm.status === 3 || auditForm.status === 4">
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

const filterPrincipal = ref('')

const principalOptions = computed(() => {
  const names = projects.value
    .filter(p => p.status === '待审核' || p.status === '待结题验收')
    .map(p => p.principalName)
    .filter(name => name != null)
  return [...new Set(names)]
})

const auditProjects = computed(() => {
  let list = projects.value.filter(p => p.status === '待审核' || p.status === '待结题验收')
  if (filterPrincipal.value) {
    list = list.filter(p => p.principalName === filterPrincipal.value)
  }
  return list
})

async function loadData() {
  const res = await projectApi.getList()
  projects.value = res.data
}

function openAudit(row) {
  currentProject.value = row
  auditForm.id = row.id
  auditForm.status = row.status === '待结题验收' ? 5 : 2
  auditForm.auditRemark = ''
  visible.value = true
}

async function handleAudit() {
  if ((auditForm.status === 3 || auditForm.status === 4) && !auditForm.auditRemark.trim()) {
    ElMessage.warning('驳回时必须填写审核指导意见')
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
.filter-area { display: flex; align-items: center; gap: 12px; }
.filter-label { font-size: 14px; color: #64748b; font-weight: 600; }
</style>
