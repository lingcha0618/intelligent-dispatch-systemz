import request from './request'
import type { UserVO, UserQueryDTO, UserCreateDTO, UserStatusDTO, IPage, Result } from '@/types'

export function getUserPage(params: UserQueryDTO) {
  return request.get<Result<IPage<UserVO>>, Result<IPage<UserVO>>>('/api/admin/user/page', { params })
}

export function createUser(data: UserCreateDTO) {
  return request.post('/api/admin/user', data)
}

export function updateUserStatus(id: number, data: UserStatusDTO) {
  return request.put(`/api/admin/user/${id}/status`, data)
}