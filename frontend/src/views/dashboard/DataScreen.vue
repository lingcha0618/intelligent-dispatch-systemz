<template>
  <div class="data-screen">
    <div class="screen-header">
      <div class="header-left">
        <el-button text @click="router.push('/admin')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回后台
        </el-button>
      </div>
      <h1 class="screen-title">校园资源智能预约调度 · 数据大屏</h1>
      <div class="header-right">
        <span class="current-time">{{ currentTime }}</span>
      </div>
    </div>

    <div class="screen-body">
      <div class="kpi-row">
        <div class="kpi-card kpi-1">
          <div class="kpi-icon">📦</div>
          <div class="kpi-info">
            <div class="kpi-value">{{ overview.resourceTotal ?? '-' }}</div>
            <div class="kpi-label">资源总数</div>
          </div>
        </div>
        <div class="kpi-card kpi-2">
          <div class="kpi-icon">🔥</div>
          <div class="kpi-info">
            <div class="kpi-value">{{ overview.occupiedCount ?? '-' }}</div>
            <div class="kpi-label">当前占用</div>
          </div>
        </div>
        <div class="kpi-card kpi-3">
          <div class="kpi-icon">📅</div>
          <div class="kpi-info">
            <div class="kpi-value">{{ overview.todayBookingCount ?? '-' }}</div>
            <div class="kpi-label">今日预约</div>
          </div>
        </div>
        <div class="kpi-card kpi-4">
          <div class="kpi-icon">⚠️</div>
          <div class="kpi-info">
            <div class="kpi-value">{{ overview.violationRate != null ? (overview.violationRate * 100).toFixed(1) + '%' : '-' }}</div>
            <div class="kpi-label">违约率</div>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card chart-occupancy">
          <h3 class="chart-title">资源占用分布</h3>
          <div ref="occupancyRef" class="chart-box"></div>
        </div>
        <div class="chart-card chart-trend">
          <h3 class="chart-title">预约趋势（近 7 天）</h3>
          <div ref="trendRef" class="chart-box"></div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card chart-forecast">
          <h3 class="chart-title">AI 热度预测（未来 24 小时）</h3>
          <div ref="forecastRef" class="chart-box"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getOverview, getOccupancy, getTrend, getForecast } from '@/api/dashboard'
import { getResourcePage } from '@/api/resource'
import type { OverviewVO, OccupancyVO, TrendPointVO, ForecastPointVO, ResourceVO } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()
const overview = ref<Partial<OverviewVO>>({})
const currentTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))

const occupancyRef = ref<HTMLElement>()
const trendRef = ref<HTMLElement>()
const forecastRef = ref<HTMLElement>()
let occupancyChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let forecastChart: echarts.ECharts | null = null

let timer: ReturnType<typeof setInterval> | null = null

async function fetchAll() {
  const [ov, occ, tr, fc, rp] = await Promise.all([
    getOverview(), getOccupancy(), getTrend(7), getForecast(undefined, 24),
    getResourcePage({ page: 1, size: 100 }),
  ])
  overview.value = ov.data
  renderOccupancy(occ.data, rp.data.records)
  renderTrend(tr.data)
  renderForecast(fc.data, rp.data.records)
}

function renderOccupancy(data: OccupancyVO[], resources: ResourceVO[]) {
  if (!occupancyRef.value) return
  if (!occupancyChart) occupancyChart = echarts.init(occupancyRef.value)
  const names = data.map((d) => {
    const r = resources.find((x) => x.id === d.resourceId)
    return r?.name || `资源${d.resourceId}`
  })
  occupancyChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 20, left: 50, right: 20, bottom: 40 },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 25, fontSize: 11, color: '#a9aeb8' } },
    yAxis: { type: 'value', axisLabel: { color: '#a9aeb8' } },
    series: [{
      type: 'bar', data: data.map((d) => d.count),
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' },
        ]),
      },
    }],
  })
}

function renderTrend(data: TrendPointVO[]) {
  if (!trendRef.value) return
  if (!trendChart) trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 20, left: 50, right: 20, bottom: 30 },
    xAxis: { type: 'category', data: data.map((d) => d.date), axisLabel: { color: '#a9aeb8' } },
    yAxis: { type: 'value', axisLabel: { color: '#a9aeb8' } },
    series: [{
      type: 'line', smooth: true, data: data.map((d) => d.count),
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(79,172,254,0.4)' }, { offset: 1, color: 'rgba(79,172,254,0.02)' },
      ]) },
      lineStyle: { width: 3, color: '#4facfe' },
      itemStyle: { color: '#4facfe' },
    }],
  })
}

function renderForecast(data: ForecastPointVO[], resources: ResourceVO[]) {
  if (!forecastRef.value) return
  if (!forecastChart) forecastChart = echarts.init(forecastRef.value)
  const grouped: Record<number, ForecastPointVO[]> = {}
  data.forEach((p) => { (grouped[p.resourceId] ||= []).push(p) })
  const times = data.length > 0 ? [...new Set(data.map((p) => dayjs(p.forecastTime).format('MM-DD HH:mm')))] : []
  const colors = ['#4facfe', '#43e97b', '#fa709a', '#ff9a2e', '#a78bfa', '#fbbf24']
  const series = Object.entries(grouped).map(([rid, points], idx) => ({
    name: resources.find((r) => r.id === Number(rid))?.name || `资源${rid}`,
    type: 'line', smooth: true,
    data: times.map((t) => {
      const p = points.find((x) => dayjs(x.forecastTime).format('MM-DD HH:mm') === t)
      return p?.heatValue ?? 0
    }),
    lineStyle: { color: colors[idx % colors.length], width: 2 },
    itemStyle: { color: colors[idx % colors.length] },
  }))
  forecastChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, textStyle: { color: '#a9aeb8', fontSize: 11 } },
    grid: { top: 40, left: 50, right: 20, bottom: 40 },
    xAxis: { type: 'category', data: times, axisLabel: { rotate: 30, fontSize: 10, color: '#a9aeb8' } },
    yAxis: { type: 'value', max: 100, axisLabel: { color: '#a9aeb8' } },
    series,
  })
}

function handleResize() {
  occupancyChart?.resize()
  trendChart?.resize()
  forecastChart?.resize()
}

onMounted(async () => {
  await nextTick()
  fetchAll()
  timer = setInterval(() => {
    currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  }, 1000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  occupancyChart?.dispose()
  trendChart?.dispose()
  forecastChart?.dispose()
})
</script>

<style scoped lang="scss">
.data-screen {
  height: 100vh;
  background: #0f1923;
  color: #e5e6eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.screen-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  background: linear-gradient(180deg, #1a2332 0%, #0f1923 100%);
  border-bottom: 1px solid #1e2936;

  .back-btn { color: #86909c; }
  .screen-title {
    font-size: 22px; font-weight: 700;
    background: linear-gradient(90deg, #4facfe, #00f2fe);
    -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  }
  .current-time { font-size: 14px; color: #86909c; font-family: monospace; }
}

.screen-body {
  flex: 1;
  padding: 20px 30px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.kpi-card {
  background: linear-gradient(135deg, #1a2332 0%, #1e2936 100%);
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #2b3744;

  .kpi-icon { font-size: 40px; }
  .kpi-value { font-size: 32px; font-weight: 800; color: #fff; }
  .kpi-label { font-size: 14px; color: #86909c; margin-top: 4px; }

  &.kpi-1 { border-left: 4px solid #4facfe; }
  &.kpi-2 { border-left: 4px solid #ff9a2e; }
  &.kpi-3 { border-left: 4px solid #43e97b; }
  &.kpi-4 { border-left: 4px solid #fa709a; }
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  flex: 1;
  min-height: 0;

  &:last-child { grid-template-columns: 1fr; }
}

.chart-card {
  background: #1a2332;
  border-radius: 12px;
  padding: 16px 20px;
  border: 1px solid #2b3744;
  display: flex;
  flex-direction: column;

  .chart-title {
    font-size: 15px; font-weight: 600; color: #c9cdd4; margin-bottom: 8px;
  }
  .chart-box { flex: 1; min-height: 240px; }
}
</style>