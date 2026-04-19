<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>支出监控</h2>
        <p class="muted">全量监控各项目的支出详细情况，并可代理录入支出记录。</p>
      </div>
      <el-button type="primary" @click="openCreate">录入支出</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="项目">
          <el-select v-model="filters.projectId" style="width: 320px" clearable filterable placeholder="按人/项目名筛选">
            <el-option-group v-for="group in groupedProjects" :key="group.label" :label="group.label">
              <el-option 
                v-for="project in group.options" 
                :key="project.id" 
                :label="project.projectName" 
                :value="project.id"
              >
                <div class="option-content">
                  <span class="proj-name">{{ project.projectName }}</span>
                  <span class="proj-code">{{ project.projectCode }}</span>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" style="width: 180px" clearable>
            <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="filters.dateRange" type="daterange" value-format="YYYY-MM-DD" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadExpenditures">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" empty-text="请选择查询条件">
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
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
        <el-form-item label="对应项目">
          <el-select v-model="editing.projectId" style="width: 100%" filterable placeholder="选择项目">
            <el-option-group v-for="group in groupedProjects" :key="group.label" :label="group.label">
              <el-option 
                v-for="project in group.options" 
                :key="project.id" 
                :label="project.projectName" 
                :value="project.id"
              >
                <div class="option-content">
                  <span class="proj-name">{{ project.projectName }}</span>
                  <span class="proj-code">{{ project.projectCode }}</span>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
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
        <el-button type="primary" @click="submit">确认保存</el-button>
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
import { formatCurrency } from '@/utils/format'

const projects = ref([])
const categories = ref([])
const rows = ref([])
const visible = ref(false)

const filters = reactive({
  projectId: undefined,
  categoryId: undefined,
  dateRange: []
})

const editing = reactive({
  projectId: undefined,
  categoryId: undefined,
  amount: 0,
  expenditureDate: new Date().toISOString().split('T')[0],
  remark: ''
})

const groupedProjects = computed(() => {
  // 不过滤状态，保留全量项目
  const groups = {}
  projects.value.forEach(p => {
    const name = p.principalName || '未知负责人'
    if (!groups[name]) {
      groups[name] = {
        label: `${name} 负责的项目`,
        options: []
      }
    }
    groups[name].options.push(p)
  })
  return Object.values(groups).sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'))
})

async function loadData() {
  const [projectRes, categoryRes] = await Promise.all([
    projectApi.getList(),
    categoryApi.getList()
  ])
  projects.value = projectRes.data
  categories.value = categoryRes.data || []
  await loadExpenditures()
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

function resetFilters() {
  Object.assign(filters, { projectId: undefined, categoryId: undefined, dateRange: [] })
  loadExpenditures()
}

function openCreate() {
  editing.amount = 0
  editing.remark = ''
  visible.value = true
}

async function submit() {
  await expenditureApi.add(editing)
  ElMessage.success('支出已保存')
  visible.value = false
  await loadExpenditures()
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #64748b; font-size: 14px; }
.option-content { display: flex; justify-content: space-between; align-items: center; width: 100%; gap: 12px; }
.proj-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }
.proj-code { color: #94a3b8; font-size: 12px; font-family: monospace; }
</style>
