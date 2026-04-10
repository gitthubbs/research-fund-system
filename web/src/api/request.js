import axios from 'axios'
import { ElLoading } from 'element-plus'
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
  // 移除了模拟适配器，使用真实HTTP请求
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
    return response.data
  },
  (error) => {
    endLoading()
    if (error?.response?.status === 401) {
      clearAuthSession()
    }
    return Promise.reject(error)
  }
)

export default request
