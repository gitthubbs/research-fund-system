<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>我的科研项目</h2>
        <p class="muted">查看您负责的项目进度，并对草稿或被驳回的项目进行提交申请。</p>
      </div>
      <el-button type="primary" @click="$router.push('/projects/add')">新增立项申请</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="projects">
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectCode" label="项目编号" width="160" />
        <el-table-column label="执行进度" width="180">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.executionRate || 0)" :stroke-width="14" />
          </template>
        </el-table-column>
        <el-table-column label="智能标签" width="160">
          <template #default="{ row }">
            <div class="smart-tags">
              <el-tag
                v-for="tag in getSmartLabels(row)"
                :key="tag.text"
                :type="tag.type"
                size="small"
                effect="dark"
                class="clickable-tag"
                @click.stop="tag.action()"
              >
                {{ tag.text }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/projects/${row.id}`)">详情</el-button>
            
            <el-button 
              v-if="row.status === '未提交' || row.status === '已驳回'" 
              link 
              type="warning" 
              @click="openEdit(row)"
            >编辑</el-button>
            
            <el-button 
              v-if="row.status === '未提交' || row.status === '已驳回'" 
              link 
              type="success" 
              @click="handleSubmit(row.id)"
            >提交申请</el-button>

            <el-button 
              v-if="row.status === '待编制预算'" 
              link 
              type="primary" 
              @click="handleConfirmBudget(row.id)"
            >确认预算</el-button>

          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 科研人员简洁编辑弹窗 -->
    <el-dialog v-model="visible" title="编辑申请信息" width="640px" destroy-on-close>
      <ProjectForm 
        ref="formRef"
        v-model="editing" 
        :is-admin="false" 
        label-position="top"
      />
      <div class="edit-tip">注意：提交审核期间无法修改任何信息。</div>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="submitting">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { projectApi } from '@/api/projectApi'
import { getUser } from '@/utils/auth'
import StatusTag from '@/components/project/StatusTag.vue'
import ProjectForm from '@/components/project/ProjectForm.vue'

const projects = ref([])
const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const router = useRouter()

// 从 auth 工具获取当前用户信息
const currentUser = getUser() || {}

const editing = reactive({
  id: null,
  projectName: '',
  projectCode: '',
  startDate: '',
  endDate: '',
  totalBudget: 0
})

async function loadData() {
  const res = await projectApi.getList()
  // 只展示自己负责的项目
  projects.value = res.data.filter(p => p.principalId === currentUser.id)
}

function openEdit(row) {
  Object.assign(editing, row)
  visible.value = true
}

async function saveEdit() {
  const valid = await formRef.value.validate()
  if (!valid) return

  const payload = {
    id: editing.id,
    projectName: editing.projectName,
    projectCode: editing.projectCode,
    startDate: editing.startDate,
    endDate: editing.endDate,
    totalBudget: editing.totalBudget
  }

  submitting.value = true
  try {
    await projectApi.update(payload)
    ElMessage.success('信息已保存')
    visible.value = false
    await loadData()
  } catch (err) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleSubmit(id) {
  await projectApi.submit(id)
  ElMessage.success('申请提交完毕，等待审核')
  await loadData()
}

async function handleConfirmBudget(id) {
  // 不再直接调用 API，而是跳转到预算编制页面进行规划
  router.push({
    path: '/budgets',
    query: { projectId: id }
  })
}

const getSmartLabels = (row) => {
  const labels = []
  if (!row.startDate || !row.endDate || row.status !== '执行中') return labels

  const now = new Date()
  const start = new Date(row.startDate)
  const end = new Date(row.endDate)
  
  // 时间进度 (0~1)
  let timeProgress = 0
  if (now > end) {
    timeProgress = 1
  } else if (now >= start) {
    const totalDuration = end.getTime() - start.getTime()
    const elapsed = now.getTime() - start.getTime()
    timeProgress = elapsed / totalDuration
  }

  // 1. 余额告急: (总预算 - 已支出) / 总预算 < 10%
  const total = Number(row.totalBudget || 0)
  const spent = Number(row.spentAmount || 0)
  if (total > 0 && (total - spent) / total < 0.1) {
    labels.push({
      type: 'danger',
      text: '余额告急',
      action: () => router.push(`/projects/${row.id}`)
    })
  }

  // 2. 进度滞后: 执行率 < 时间进度 * 40% (其中时间进度转为百分比)
  // 增加时间进度门槛 (0.1)，防止新立项项目误报
  const execRate = Number(row.executionRate || 0)
  if (timeProgress > 0.1 && execRate < timeProgress * 40) {
    labels.push({
      type: 'warning',
      text: '进度滞后',
      action: () => router.push({ path: `/projects/${row.id}` })
    })
  }

  return labels
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #64748b; font-size: 14px; margin: 4px 0 0; }
.edit-tip { margin-top: 12px; color: #94a3b8; font-size: 13px; font-style: italic; }
.status-tip { margin-left: 12px; }
.smart-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.clickable-tag { cursor: pointer; transition: transform 0.2s; }
.clickable-tag:hover { transform: scale(1.05); filter: brightness(1.1); }
</style>
