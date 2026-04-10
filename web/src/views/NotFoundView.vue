<template>
  <div class="not-found">
    <el-result
      icon="info"
      title="404"
      sub-title="页面不存在或地址已失效。"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 60vh;
  display: grid;
  place-items: center;
}
</style>
