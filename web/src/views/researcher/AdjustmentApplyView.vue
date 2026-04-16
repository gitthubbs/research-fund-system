<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>预算调剂申请</h2>
        <p class="muted">申请将经费在不同科目之间进行划转调拨</p>
      </div>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form :model="form" label-position="top" ref="applyForm" :rules="rules">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择要调剂的项目" @change="handleProjectChange" style="width: 100%">
                <el-option
                  v-for="p in myProjects"
                  :key="p.id"
                  :label="p.projectName"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="40" v-if="form.projectId">
          <!-- 调出部分 -->
          <el-col :span="11">
            <div class="adj-box from-box">
              <div class="box-header">调出科目 (From)</div>
              <el-form-item label="科目名称" prop="fromCategoryId">
                <el-select v-model="form.fromCategoryId" placeholder="划转出科目" @change="loadAvailableBalance" style="width: 100%">
                  <el-option
                    v-for="b in projectBudgets"
                    :key="b.categoryId"
                    :label="b.categoryName"
                    :value="b.categoryId"
                  />
                </el-select>
              </el-form-item>
              <div class="balance-info" v-if="form.fromCategoryId">
                当前预算: {{ formatCurrency(currentFromBudget.budgetAmount) }}<br/>
                可用余额: <span class="available">{{ formatCurrency(availableBalance) }}</span>
              </div>
            </div>
          </el-col>

          <el-col :span="2" class="arrow-col">
            <el-icon :size="24"><Right /></el-icon>
          </el-col>

          <!-- 调入部分 -->
          <el-col :span="11">
            <div class="adj-box to-box">
              <div class="box-header">调入科目 (To)</div>
              <el-form-item label="科目名称" prop="toCategoryId">
                <el-select v-model="form.toCategoryId" placeholder="划转入科目" style="width: 100%">
                  <el-option
                    v-for="b in projectBudgets"
                    :key="b.categoryId"
                    :label="b.categoryName"
                    :value="b.categoryId"
                    :disabled="b.categoryId === form.fromCategoryId"
                  />
                </el-select>
              </el-form-item>
              <div class="balance-info" v-if="form.toCategoryId">
                当前预算: {{ formatCurrency(currentToBudget.budgetAmount) }}
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.projectId" style="margin-top: 20px">
          <el-col :span="12">
            <el-form-item label="调剂金额" prop="amount">
              <el-input-number 
                v-model="form.amount" 
                :precision="2" 
                :step="1000" 
                :min="0" 
                :max="Number(availableBalance)"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="申请原因" prop="reason" v-if="form.projectId">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请详细说明调剂的必要性..." />
        </el-form-item>

        <el-form-item style="margin-top: 30px">
          <el-button type="primary" size="large" @click="submitApply" :loading="submitting" :disabled="!form.projectId">提交调剂申请</el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="tips" v-if="!form.projectId">
      <el-empty description="请先在上方选择一个科研项目" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Right } from '@element-plus/icons-vue'
import { projectApi } from '@/api/projectApi'
import { budgetApi } from '@/api/budgetApi'
import { adjustmentApi } from '@/api/adjustmentApi'
import { getUser } from '@/utils/auth'
import { formatCurrency } from '@/utils/format'

const router = useRouter()
const currentUser = getUser()
const applyForm = ref(null)
const submitting = ref(false)

const myProjects = ref([])
const projectBudgets = ref([])
const availableBalance = ref(0)

const form = reactive({
  projectId: null,
  fromCategoryId: null,
  toCategoryId: null,
  amount: 0,
  reason: '',
  applicantId: currentUser.id
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  fromCategoryId: [{ required: true, message: '请选择调出科目', trigger: 'change' }],
  toCategoryId: [{ required: true, message: '请选择调入科目', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入调剂金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '调剂金额必须大于 0', trigger: 'blur' }
  ],
  reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
}

const currentFromBudget = computed(() => projectBudgets.value.find(b => b.categoryId === form.fromCategoryId) || {})
const currentToBudget = computed(() => projectBudgets.value.find(b => b.categoryId === form.toCategoryId) || {})

async function loadMyProjects() {
  const res = await projectApi.getList()
  // 只展示自己负责且正在执行的项目
  // 注意：VO 中的 status 是格式化后的字符串
  myProjects.value = res.data.filter(p => p.principalId === currentUser.id && p.status === '执行中')
}

async function handleProjectChange(val) {
  form.fromCategoryId = null
  form.toCategoryId = null
  form.amount = 0
  availableBalance.value = 0
  
  if (val) {
    const res = await budgetApi.getByProject(val)
    projectBudgets.value = res.data
  }
}

async function loadAvailableBalance() {
  if (form.projectId && form.fromCategoryId) {
    const res = await budgetApi.getAvailableBalance(form.projectId, form.fromCategoryId)
    availableBalance.value = res.data
  }
}

async function submitApply() {
  const valid = await applyForm.value.validate()
  if (!valid) return

  if (form.fromCategoryId === form.toCategoryId) {
    return ElMessage.warning('调出和调入科目不能为同一个')
  }

  submitting.value = true
  try {
    await adjustmentApi.apply(form)
    ElMessage.success('申请提交成功，请等待管理员审核')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadMyProjects)
</script>

<style scoped>
.page { display: grid; gap: 20px; max-width: 1000px; margin: 0 auto; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #64748b; font-size: 14px; margin: 4px 0 0; }
.form-card { padding: 10px; }

.adj-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}
.box-header {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 12px;
  color: #1e293b;
}
.from-box { border-left: 4px solid #ef4444; }
.to-box { border-left: 4px solid #10b981; }

.arrow-col {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #94a3b8;
}

.balance-info {
  margin-top: 12px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}
.available {
  color: #ef4444;
  font-weight: 700;
}
.tips { margin-top: 40px; }
</style>
