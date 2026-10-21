import request from './request'
import type { LoginDTO, LoginVO, RegisterDTO, Result } from '@/types'

export function login(data: LoginDTO) {
  return request.post<Result<LoginVO>, Result<LoginVO>>('/api/auth/login', data)
}

export function register(data: RegisterDTO) {
  return request.post<Result<void>, Result<void>>('/api/auth/register', data)
}