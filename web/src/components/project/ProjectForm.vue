<template>
  <el-form :model="localModel" :rules="rules" ref="formRef" :label-position="labelPosition" :label-width="labelWidth">
    <el-row :gutter="gutter">
      <el-col :span="24">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="localModel.projectName" placeholder="请输入完整的项目名称" />
        </el-form-item>
      </el-col>
      
      <el-col :span="12">
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="localModel.projectCode" placeholder="如：PRJ-2024-XXX" />
        </el-form-item>
      </el-col>
      
      <el-col :span="12" v-if="isAdmin">
        <el-form-item label="负责人" prop="principalId">
          <el-select v-model="localModel.principalId" style="width: 100%" placeholder="请选择负责人">
            <el-option v-for="user in researchers" :key="user.id" :label="user.name" :value="user.id" />
          </el-select>
        </el-form-item>
      </el-col>

      <el-col :span="isAdmin ? 12 : 24">
        <el-form-item label="总预算 (元)" prop="totalBudget">
          <el-input-number 
            v-model="localModel.totalBudget" 
            :precision="2" 
            :step="1000" 
            :min="0" 
            style="width: 100%" 
          />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="localModel.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="localModel.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>


    </el-row>
  </el-form>
</template>

<script setup>
import { reactive, watch, ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  isAdmin: {
    type: Boolean,
    default: false
  },
  researchers: {
    type: Array,
    default: () => []
  },
  labelPosition: {
    type: String,
    default: 'top'
  },
  labelWidth: {
    type: String,
    default: '100px'
  },
  gutter: {
    type: Number,
    default: 20
  }
})

const emit = defineEmits(['update:modelValue'])

const formRef = ref(null)
const localModel = reactive({ ...props.modelValue })

// 深度同步到父组件
watch(localModel, (newVal) => {
  emit('update:modelValue', newVal)
}, { deep: true })

// 监听外界数据变化（如打开编辑时）
watch(() => props.modelValue, (newVal) => {
  Object.assign(localModel, newVal)
}, { deep: true })

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  principalId: [{ required: true, message: '请选择负责人', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  totalBudget: [{ required: true, message: '请输入总预算', trigger: 'blur' }]
}

// 供父组件调用的验证方法
const validate = () => formRef.value.validate()

defineExpose({
  validate
})
</script>
