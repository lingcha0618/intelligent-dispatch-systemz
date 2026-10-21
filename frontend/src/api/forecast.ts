import request from './request'

export function triggerForecast() {
  return request.post('/api/admin/forecast/trigger')
}