<template>
  <div class="page">
    <div class="header">
      <h2>报销录入</h2>
      <p class="muted">请填写详细的支出信息并提交审核</p>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="top"
        size="large"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select
                v-model="form.projectId"
                placeholder="请选择项目"
                filterable
                @change="handleProjectChange"
              >
                <el-option
                  v-for="item in projects"
                  :key="item.id"
                  :label="item.projectName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支出科目" prop="categoryId">
              <el-select
                v-model="form.categoryId"
                placeholder="请选择科目"
                :disabled="!form.projectId"
                @change="fetchBalance"
              >
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.categoryName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 余额提示区 -->
        <transition name="el-fade-in">
          <div v-if="balanceInfo.show" class="balance-alert">
            <el-alert
              :title="`该科目当前可用余额：¥ ${balanceInfo.amount}`"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </transition>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="报销金额 (元)" prop="amount">
              <el-input-number
                v-model="form.amount"
                :precision="2"
                :step="100"
                :min="0.01"
                placeholder="请输入金额"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支出日期" prop="expenditureDate">
              <el-date-picker
                v-model="form.expenditureDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请简要描述支出用途"
          />
        </el-form-item>

        <div class="actions">
          <el-button @click="$router.push('/dashboard')">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            提交报销
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/projectApi'
import { categoryApi } from '@/api/categoryApi'
import { budgetApi } from '@/api/budgetApi'
import { expenditureApi } from '@/api/expenditureApi'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)
const projects = ref([])
const categories = ref([])
const balanceInfo = reactive({
  show: false,
  amount: 0
})

const form = reactive({
  projectId: null,
  categoryId: null,
  amount: null,
  expenditureDate: new Date().toISOString().split('T')[0],
  remark: ''
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  amount: [{ required: true, message: '请输入报销金额', trigger: 'blur' }],
  expenditureDate: [{ required: true, message: '请选择支出日期', trigger: 'change' }]
}

async function loadInitialData() {
  try {
    const projRes = await projectApi.getList()
    // ★ 过滤：报销录入只允许选择“执行中”的项目
    projects.value = (projRes.data || []).filter(item => item.status === '执行中')
    const catRes = await categoryApi.getList()
    categories.value = catRes.data

    // 如果 URL 参数中带有项目 ID，则自动预选
    const pid = route.query.projectId
    if (pid && projects.value.some(p => p.id === Number(pid))) {
      form.projectId = Number(pid)
    }
  } catch (error) {
    ElMessage.error('基础数据加载失败')
  }
}

function handleProjectChange() {
  form.categoryId = null
  balanceInfo.show = false
}

async function fetchBalance() {
  if (form.projectId && form.categoryId) {
    try {
      const response = await budgetApi.getAvailableBalance(form.projectId, form.categoryId)
      balanceInfo.amount = response.data
      balanceInfo.show = true
    } catch (error) {
      console.error('获取余额失败', error)
      balanceInfo.show = false
    }
  }
}

async function submitForm() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await expenditureApi.create(form)
        ElMessage.success('报销申请已提交，等待审核')
        router.push('/expenditures')
      } catch (error) {
        // 后端拦截逻辑会返回 400/500 错误及详细提示
        const errorMsg = error.response?.data?.message || error.message || '提交失败'
        if (errorMsg.includes('决策建议')) {
          ElMessageBox.alert(errorMsg.replace(/\n\n/g, '<br/><br/>'), '预算校验未通过', {
            confirmButtonText: '我知道了',
            type: 'warning',
            dangerouslyUseHTMLString: true,
            center: true
          })
        } else {
          ElMessage.error(errorMsg)
        }
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(loadInitialData)
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  gap: 24px;
}

.header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.muted {
  color: #64748b;
}

.form-card {
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.balance-alert {
  margin-top: -8px;
  margin-bottom: 24px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px dashed rgba(148, 163, 184, 0.2);
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
}

:deep(.el-select) {
  width: 100%;
}
</style>
