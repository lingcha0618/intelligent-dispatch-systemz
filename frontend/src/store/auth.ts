import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginVO } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userId = ref<number>(Number(localStorage.getItem('userId')) || 0)
  const realName = ref<string>(localStorage.getItem('realName') || '')
  const role = ref<string>(localStorage.getItem('role') || '')

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN' || role.value === 'SUPER_ADMIN')
  const isSuperAdmin = computed(() => role.value === 'SUPER_ADMIN')

  function setAuth(data: LoginVO) {
    token.value = data.token
    userId.value = data.userId
    realName.value = data.realName
    role.value = data.role
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('realName', data.realName)
    localStorage.setItem('role', data.role)
  }

  function logout() {
    token.value = ''
    userId.value = 0
    realName.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('realName')
    localStorage.removeItem('role')
  }

  return { token, userId, realName, role, isLogin, isAdmin, isSuperAdmin, setAuth, logout }
})