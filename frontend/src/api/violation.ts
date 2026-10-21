import request from './request'
import type { ViolationVO, ViolationQueryDTO, ViolationCreateDTO, IPage, Result } from '@/types'

export function getViolationPage(params: ViolationQueryDTO) {
  return request.get<Result<IPage<ViolationVO>>, Result<IPage<ViolationVO>>>('/api/admin/violation/page', { params })
}

export function createViolation(data: ViolationCreateDTO) {
  return request.post('/api/admin/violation', data)
}