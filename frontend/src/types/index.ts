// ===================== 枚举常量 =====================

export const RoleMap = { 0: '学生', 1: '管理员', 2: '超级管理员' } as const
export const ResourceTypeMap = { 1: '教室', 2: '图书馆座位', 3: '实验室', 4: '会议室' } as const
export const ResourceStatusMap = { 0: '空闲', 1: '占用', 2: '维护中' } as const
export const BookingStatusMap = { 0: '待签到', 1: '使用中', 2: '已完成', 3: '已取消', 4: '违约' } as const
export const NoticeTypeMap = { 1: '预约成功', 2: '到期提醒', 3: '违约通知', 4: '系统公告' } as const
export const ViolationTypeMap = { 1: '超时未签到', 2: '早退', 3: '恶意占座' } as const
export const UserStatusMap = { 0: '正常', 1: '封禁' } as const

export const ResourceTypeIcon: Record<number, string> = {
  1: '🏫', 2: '📚', 3: '🔬', 4: '🏢',
}

// ===================== 通用响应 =====================

export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

export interface IPage<T> {
  size: number
  total: number
  records: T[]
  current: number
  pages: number
}

// ===================== 认证 =====================

export interface LoginDTO {
  username: string
  password: string
}

export interface LoginVO {
  token: string
  userId: number
  realName: string
  role: 'STUDENT' | 'ADMIN' | 'SUPER_ADMIN'
}

export interface RegisterDTO {
  username: string
  password: string
  realName: string
  phone?: string
}

// ===================== 用户 =====================

export interface UserVO {
  id: number
  username: string
  realName: string
  role: number
  roleDesc: string
  phone: string
  status: number
  banUntil: string | null
}

export interface UserQueryDTO {
  page?: number
  size?: number
  keyword?: string
  role?: number
}

export interface UserCreateDTO {
  username: string
  password: string
  realName: string
  role: number
  phone?: string
}

export interface UserStatusDTO {
  status: number
  banUntil?: string
}

// ===================== 资源 =====================


export interface ResourceVO {
  id: number
  name: string
  type: number
  typeDesc: string
  location: string
  capacity: number
  openStart: string | null
  openEnd: string | null
  status: number
  statusDesc: string
  description: string
  heatValue: number | null
}

export interface SlotVO {
  startTime: string
  endTime: string
  bookable: boolean
}

export interface ResourceDetailVO extends ResourceVO {
  slots: SlotVO[]
}

export interface ResourceQueryDTO {
  page?: number
  size?: number
  type?: number
  status?: number
}

export interface ResourceDTO {
  name: string
  type: number
  location?: string
  capacity: number
  openStart?: string | null
  openEnd?: string | null
  status?: number
  description?: string
}

// ===================== 预约 =====================

export interface BookingVO {
  id: number
  userId: number
  resourceId: number
  resourceName: string
  resourceType: number
  resourceTypeDesc: string
  requestId: string
  startTime: string
  endTime: string
  status: number
  statusDesc: string
  checkInTime: string | null
  createTime: string
}

export interface BookingQueryDTO {
  page?: number
  size?: number
  status?: number
  resourceId?: number
  userId?: number
}

export interface BookingCreateDTO {
  requestId: string
  resourceId: number
  startTime?: string
  endTime?: string
}

export interface IdempotentTokenVO {
  requestId: string
}

// ===================== 通知 =====================

export interface NoticeVO {
  id: number
  title: string
  content: string
  type: number
  typeDesc: string
  isRead: number
  createTime: string
}

export interface NoticeQueryDTO {
  page?: number
  size?: number
  isRead?: number
}

export interface AnnouncementDTO {
  title: string
  content: string
}

// ===================== 违约 =====================

export interface ViolationVO {
  id: number
  userId: number
  bookingId: number
  type: number
  typeDesc: string
  createTime: string
}

export interface ViolationQueryDTO {
  page?: number
  size?: number
  userId?: number
  type?: number
}

export interface ViolationCreateDTO {
  bookingId: number
  type: number
}

// ===================== 大屏 =====================

export interface OverviewVO {
  resourceTotal: number
  occupiedCount: number
  todayBookingCount: number
  violationRate: number
}

export interface OccupancyVO {
  resourceId: number
  count: number
}

export interface TrendPointVO {
  date: string
  count: number
}

export interface ForecastPointVO {
  resourceId: number
  forecastTime: string
  heatValue: number
}

// ===================== 配置 =====================

export interface ConfigItemDTO {
  key: string
  value: string
}

// ===================== 导出 =====================

export interface ExportQueryDTO {
  startDate?: string
  endDate?: string
  format?: string
}