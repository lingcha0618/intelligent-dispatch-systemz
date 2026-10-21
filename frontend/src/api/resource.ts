import request from './request'
import type { ResourceQueryDTO, ResourceVO, ResourceDetailVO, ResourceDTO, IPage, Result } from '@/types'

export function getResourcePage(params: ResourceQueryDTO) {
  return request.get<Result<IPage<ResourceVO>>, Result<IPage<ResourceVO>>>('/api/resource/page', { params })
}

export function getResourceDetail(id: number) {
  return request.get<Result<ResourceDetailVO>, Result<ResourceDetailVO>>(`/api/resource/${id}`)
}

export function getResourceSlots(id: number) {
  return request.get(`/api/resource/${id}/slots`)
}

export function createResource(data: ResourceDTO) {
  return request.post('/api/admin/resource', data)
}

export function updateResource(id: number, data: ResourceDTO) {
  return request.put(`/api/admin/resource/${id}`, data)
}

export function deleteResource(id: number) {
  return request.delete(`/api/admin/resource/${id}`)
}