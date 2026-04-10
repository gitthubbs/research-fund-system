<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>支出监控</h2>
        <p class="muted">按项目、分类、时间范围查询并录入支出记录。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增支出</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="项目">
          <el-select v-model="filters.projectId" style="width: 220px" clearable>
            <el-option v-for="project in projects" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
        </el-form-item>
        <!-- ★ 新增 -->
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" style="width: 200px" clearable>
            <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
          </el-select>
        </el-form-item>
        <!-- ★ 新增 -->
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <!-- ★ 新增 -->
        <el-form-item>
          <el-button type="primary" @click="loadExpenditures">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" empty-text="暂无数据">
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="categoryName" label="支出类别" width="140" />
        <el-table-column label="支出金额" width="140">
          <template #default="{ row }">{{ formatCurrency(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="expenditureDate" label="支出日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="240" />
      </el-table>
    </el-card>

    <!-- ★ 新增 -->
    <el-dialog v-model="visible" title="新增支出" width="520px">
      <el-form :model="editing" label-width="90px">
        <el-form-item label="项目">
          <el-select v-model="editing.projectId" style="width: 100%">
            <el-option v-for="project in projects" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editing.categoryId" style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="editing.amount" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="editing.expenditureDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editing.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { expenditureApi } from '@/api/expenditureApi'
import { projectApi } from '@/api/projectApi'
import { categoryApi } from '@/api/categoryApi'
import { getRole, getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'

const role = getRole()
const currentUser = getUser()
const projects = ref([])
// ★ 新增
const categories = ref([])
const rows = ref([])
// ★ 新增
const visible = ref(false)

// ★ 新增
const filters = reactive({
  projectId: undefined,
  categoryId: undefined,
  dateRange: []
})

// ★ 新增
const editing = reactive({
  projectId: undefined,
  categoryId: undefined,
  amount: 0,
  expenditureDate: '',
  remark: ''
})

async function loadProjects() {
  const response = await projectApi.getList()
  projects.value = role === 'researcher'
    ? response.data.filter((item) => item.principalId === currentUser?.id)
    : response.data

  if (!filters.projectId && projects.value.length) {
    filters.projectId = projects.value[0].id
  }
  if (!editing.projectId && projects.value.length) {
    editing.projectId = projects.value[0].id
  }
}

// ★ 新增
async function loadCategories() {
  const response = await categoryApi.getList()
  categories.value = response.data || []
  if (!editing.categoryId && categories.value.length) {
    editing.categoryId = categories.value[0].id
  }
}

async function loadExpenditures() {
  const params = {
    projectId: filters.projectId,
    categoryId: filters.categoryId,
    startDate: filters.dateRange?.[0],
    endDate: filters.dateRange?.[1]
  }
  const response = await expenditureApi.search(params)
  rows.value = response.data || []
}

// ★ 新增
function resetFilters() {
  filters.categoryId = undefined
  filters.dateRange = []
  loadExpenditures()
}

// ★ 新增
function openCreate() {
  editing.amount = 0
  editing.expenditureDate = ''
  editing.remark = ''
  visible.value = true
}

// ★ 新增
async function submit() {
  await expenditureApi.add(editing)
  ElMessage.success('支出已新增')
  visible.value = false
  await loadExpenditures()
}

onMounted(async () => {
  await Promise.all([loadProjects(), loadCategories()])
  await loadExpenditures()
})
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.muted {
  color: #64748b;
}
</style>
