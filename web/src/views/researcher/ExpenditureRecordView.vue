<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>支出录入与查询</h2>
        <p class="muted">录入您负责项目的经费支出，并查询历史明细。</p>
      </div>
      <el-button type="primary" @click="openCreate" :disabled="!hasActiveProjects">录入支出</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="项目">
          <el-select v-model="filters.projectId" style="width: 260px" clearable @change="loadExpenditures">
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

      <el-table :data="rows" empty-text="当前选定项目暂无支出记录">
        <el-table-column prop="categoryName" label="支出类别" width="140" />
        <el-table-column label="支出金额" width="140">
          <template #default="{ row }">{{ formatCurrency(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="expenditureDate" label="支出日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="新增支出记录" width="520px" destroy-on-close>
      <el-form :model="editing" label-width="90px">
        <el-form-item label="所属项目">
          <el-select v-model="editing.projectId" style="width: 100%">
            <el-option 
              v-for="project in activeProjects" 
              :key="project.id" 
              :label="project.projectName" 
              :value="project.id" 
            />
          </el-select>
          <p class="tip" v-if="!activeProjects.length">暂无已进入“执行中”阶段的项目</p>
        </el-form-item>
        <el-form-item label="支出分类">
          <el-select v-model="editing.categoryId" style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="支出金额">
          <el-input-number v-model="editing.amount" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支出日期">
          <el-date-picker v-model="editing.expenditureDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="editing.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { expenditureApi } from '@/api/expenditureApi'
import { projectApi } from '@/api/projectApi'
import { categoryApi } from '@/api/categoryApi'
import { getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'

const projects = ref([])
const categories = ref([])
const rows = ref([])
const visible = ref(false)
const submitting = ref(false)

const currentUser = getUser() || {}

const activeProjects = computed(() => {
  return projects.value.filter(p => p.status === '执行中')
})

const hasActiveProjects = computed(() => activeProjects.value.length > 0)

const filters = reactive({
  projectId: undefined
})

const editing = reactive({
  projectId: undefined,
  categoryId: undefined,
  amount: 0,
  expenditureDate: new Date().toISOString().split('T')[0],
  remark: ''
})

async function loadData() {
  const [projectRes, categoryRes] = await Promise.all([
    projectApi.getList(),
    categoryApi.getList()
  ])
  // 只过滤由于该用户负责的项目
  projects.value = projectRes.data.filter(p => p.principalId === currentUser.id)
  categories.value = categoryRes.data || []
  
  if (projects.value.length) {
    filters.projectId = projects.value[0].id
    await loadExpenditures()
  }
}

async function loadExpenditures() {
  if (!filters.projectId) return
  const params = {
    projectId: filters.projectId
  }
  const response = await expenditureApi.search(params)
  rows.value = response.data || []
}

function openCreate() {
  editing.amount = 0
  editing.remark = ''
  editing.projectId = activeProjects.value[0]?.id
  visible.value = true
}

async function submit() {
  if (!editing.projectId) {
    ElMessage.warning('请选择处于[执行中]状态的项目')
    return
  }
  submitting.value = true
  try {
    await expenditureApi.add(editing)
    ElMessage.success('支出已保存')
    visible.value = false
    await loadExpenditures()
  } catch (err) {
    ElMessage.error('保存失败')
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
.tip { margin-top: 8px; font-size: 13px; color: #f56c6c; }
</style>
