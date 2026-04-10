<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>用户管理</h2>
        <p>支持列表维护、角色切换和基础信息编辑。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增用户</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="users" empty-text="暂无数据">
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="department" label="所属部门" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'success'">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="switchRole(row)">
              切换为{{ row.role === 'admin' ? '科研人员' : '管理员' }}
            </el-button>
            <el-popconfirm title="确认删除该用户？" @confirm="removeUser(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="editing.id ? '编辑用户' : '新增用户'" width="520px">
      <el-form :model="editing" label-width="96px">
        <el-form-item label="账号"><el-input v-model="editing.username" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="editing.name" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="editing.department" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editing.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editing.email" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editing.role">
            <el-option label="管理员" value="admin" />
            <el-option label="科研人员" value="researcher" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码"><el-input v-model="editing.password" type="password" show-password /></el-form-item>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/userApi'
import { requireRelogin } from '@/utils/auth'
import { roleLabel } from '@/utils/format'

const router = useRouter()
const users = ref([])
const visible = ref(false)
const editing = reactive({
  id: null,
  username: '',
  name: '',
  department: '',
  phone: '',
  email: '',
  role: 'researcher',
  password: ''
})

function resetEditing() {
  Object.assign(editing, {
    id: null,
    username: '',
    name: '',
    department: '',
    phone: '',
    email: '',
    role: 'researcher',
    password: ''
  })
}

async function loadUsers() {
  const response = await userApi.getList()
  users.value = response.data
}

function openCreate() {
  resetEditing()
  visible.value = true
}

function openEdit(row) {
  Object.assign(editing, { ...row, password: '' })
  visible.value = true
}

async function submit() {
  if (editing.id) {
    await userApi.update(editing)
    ElMessage.success('用户已更新')
  } else {
    await userApi.add(editing)
    ElMessage.success('用户已新增')
  }
  visible.value = false
  await loadUsers()
}

async function removeUser(id) {
  await userApi.delete(id)
  ElMessage.success('用户已删除')
  await loadUsers()
}

async function switchRole(row) {
  const targetRole = row.role === 'admin' ? 'researcher' : 'admin'
  await ElMessageBox.confirm('角色变更后将清除登录态，并要求重新登录验证。是否继续？', '角色切换', {
    type: 'warning'
  })
  await userApi.switchRole(row.id, targetRole)
  requireRelogin()
  ElMessage.success('角色已切换，请重新登录')
  router.push('/login')
}

onMounted(loadUsers)
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar p {
  color: #64748b;
}
</style>
