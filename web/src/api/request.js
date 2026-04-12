import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { clearAuthSession, getToken } from '@/utils/auth'

let loadingInstance = null
let loadingCount = 0

function startLoading() {
  loadingCount += 1
  if (!loadingInstance) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(10, 18, 30, 0.18)'
    })
  }
}

function endLoading() {
  loadingCount = Math.max(loadingCount - 1, 0)
  if (loadingCount === 0 && loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    startLoading()
    const token = getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    endLoading()
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    endLoading()
    const res = response.data
    
    // 如果返回 code 不为 200，说明是业务逻辑错误
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    
    return res
  },
  (error) => {
    endLoading()
    
    // 处理 HTTP 错误
    const msg = error.response?.data?.message || error.message || '网络连接异常'
    ElMessage.error(msg)

    if (error?.response?.status === 401) {
      clearAuthSession()
    }
    return Promise.reject(error)
  }
)

export default request
