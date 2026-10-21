import request from './request'
import type { ConfigItemDTO, Result } from '@/types'

export function getAllConfig() {
  return request.get<Result<Record<string, string>>, Result<Record<string, string>>>('/api/admin/config')
}

export function updateConfig(data: ConfigItemDTO[]) {
  return request.put('/api/admin/config', data)
}