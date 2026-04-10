<template>
  <div class="forbidden">
    <el-result
      icon="warning"
      title="403"
      sub-title="当前账号没有访问该页面的权限。"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.forbidden {
  min-height: 60vh;
  display: grid;
  place-items: center;
}
</style>
