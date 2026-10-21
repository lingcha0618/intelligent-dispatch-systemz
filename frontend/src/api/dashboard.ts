import request from './request'
import type { OverviewVO, OccupancyVO, TrendPointVO, ForecastPointVO, Result } from '@/types'

export function getOverview() {
  return request.get<Result<OverviewVO>, Result<OverviewVO>>('/api/dashboard/overview')
}

export function getOccupancy(resourceId?: number) {
  return request.get<Result<OccupancyVO[]>, Result<OccupancyVO[]>>('/api/dashboard/occupancy', {
    params: resourceId ? { resourceId } : {},
  })
}

export function getTrend(days?: number) {
  return request.get<Result<TrendPointVO[]>, Result<TrendPointVO[]>>('/api/dashboard/trend', {
    params: days ? { days } : {},
  })
}

export function getForecast(resourceId?: number, hours?: number) {
  return request.get<Result<ForecastPointVO[]>, Result<ForecastPointVO[]>>('/api/dashboard/forecast', {
    params: { ...(resourceId ? { resourceId } : {}), ...(hours ? { hours } : {}) },
  })
}