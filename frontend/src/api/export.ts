import request from './request'
import type { ExportQueryDTO } from '@/types'

export function exportBookings(params: ExportQueryDTO) {
  return request.get('/api/admin/export/bookings', {
    params,
    responseType: 'blob',
  })
}