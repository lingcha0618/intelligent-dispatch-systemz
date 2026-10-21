import dayjs from 'dayjs'

export function formatDateTime(val: string | null | undefined): string {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD HH:mm')
}

export function formatDate(val: string | null | undefined): string {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD')
}

export function formatTime(val: string | null | undefined): string {
  if (!val) return '-'
  return dayjs(val).format('HH:mm')
}

export function formatLocalTime(t: string | { hour: number; minute: number } | null | undefined): string {
  if (!t) return '-'
  if (typeof t === 'string') return t
  const h = String(t.hour).padStart(2, '0')
  const m = String(t.minute).padStart(2, '0')
  return `${h}:${m}`
}

export function downloadBlob(data: Blob, filename: string) {
  const url = window.URL.createObjectURL(data)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}