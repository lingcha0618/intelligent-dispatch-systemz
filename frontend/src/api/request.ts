import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Result } from '@/types'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 15000,
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    if (response.config.responseType === 'blob') {
      return response as unknown as Result
    }
    if (res.code === 200 || res.code === 0) {
      return res
    }
    if (res.code === 401) {
      ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        localStorage.clear()
        window.location.href = '/login'
      }).catch(() => {})
      return Promise.reject(new Error(res.message || '未授权'))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    if (status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    } else if (status === 403) {
      ElMessage.error('没有权限访问该资源')
    } else if (status === 404) {
      ElMessage.error('请求的资源不存在')
    } else if (status === 500) {
      ElMessage.error(data?.message || '服务器内部错误，请稍后重试')
    } else if (data?.message) {
      ElMessage.error(data.message)
    } else {
      ElMessage.error(error.message || '网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  },
)

export default service