import request from './request'
import type { BookingQueryDTO, BookingVO, BookingCreateDTO, IdempotentTokenVO, IPage, Result } from '@/types'

export function getIdempotentToken() {
  return request.post<Result<IdempotentTokenVO>, Result<IdempotentTokenVO>>('/api/booking/idempotent-token')
}

export function createBooking(data: BookingCreateDTO) {
  return request.post<Result<BookingVO>, Result<BookingVO>>('/api/booking/create', data)
}

export function checkinBooking(id: number) {
  return request.post(`/api/booking/${id}/checkin`)
}

export function cancelBooking(id: number) {
  return request.post(`/api/booking/${id}/cancel`)
}

export function getMyBookings(params: BookingQueryDTO) {
  return request.get<Result<IPage<BookingVO>>, Result<IPage<BookingVO>>>('/api/booking/my', { params })
}

export function getBookingPage(params: BookingQueryDTO) {
  return request.get<Result<IPage<BookingVO>>, Result<IPage<BookingVO>>>('/api/admin/booking/page', { params })
}