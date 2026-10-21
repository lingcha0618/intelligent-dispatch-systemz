import request from './request'
import type { NoticeVO, NoticeQueryDTO, AnnouncementDTO, IPage, Result } from '@/types'

export function getNoticePage(params: NoticeQueryDTO) {
  return request.get<Result<IPage<NoticeVO>>, Result<IPage<NoticeVO>>>('/api/notice/page', { params })
}

export function readNotice(id: number) {
  return request.post(`/api/notice/${id}/read`)
}

export function readAllNotices() {
  return request.post('/api/notice/read-all')
}

export function sendAnnouncement(data: AnnouncementDTO) {
  return request.post('/api/admin/notice/announcement', data)
}