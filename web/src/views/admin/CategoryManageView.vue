<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>经费科目维护</h2>
        <p class="muted">定义全校通用的科研经费预算科目模版。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增科目</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="categories" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="科目名称" min-width="200" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该科目吗？如果该科目已被项目预算关联，删除可能导致显示异常。" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="isEdit ? '编辑科目' : '新增科目'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="科目名称">
          <el-input v-model="editing.categoryName" placeholder="例如：差旅费、劳务费" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { categoryApi } from '@/api/categoryApi'

const categories = ref([])
const visible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

const editing = reactive({
  id: null,
  categoryName: ''
})

async function loadData() {
  const response = await categoryApi.getList()
  categories.value = response.data || []
}

function openCreate() {
  isEdit.value = false
  editing.id = null
  editing.categoryName = ''
  visible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editing.id = row.id
  editing.categoryName = row.categoryName
  visible.value = true
}

async function handleSubmit() {
  if (!editing.categoryName.trim()) {
    ElMessage.warning('请输入科目名称')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await categoryApi.update(editing)
      ElMessage.success('科目已更新')
    } else {
      await categoryApi.add(editing)
      ElMessage.success('科目已添加')
    }
    visible.value = false
    await loadData()
  } catch (err) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await categoryApi.delete(id)
    ElMessage.success('科目已删除')
    await loadData()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #64748b; font-size: 14px; }
</style>
