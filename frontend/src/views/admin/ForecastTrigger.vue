<template>
  <div class="page-container">
    <h2 class="page-title">AI 热度预测</h2>

    <el-card class="trigger-card">
      <div class="trigger-content">
        <div class="trigger-icon">🤖</div>
        <div class="trigger-info">
          <h3>触发 AI 预测</h3>
          <p>系统将基于历史预约数据，预测各资源未来 24 小时的热度趋势，用于数据大屏展示和资源推荐。</p>
        </div>
        <el-button type="primary" size="large" :loading="triggerLoading" @click="handleTrigger">
          <el-icon><Cpu /></el-icon>
          立即触发
        </el-button>
      </div>
    </el-card>

    <el-card class="forecast-card" v-loading="chartLoading">
      <template #header>
        <div class="card-header">
          <span>预测趋势（未来 24 小时）</span>
          <el-select v-model="selectedResource" placeholder="选择资源" clearable @change="fetchForecast" style="width: 200px">
            <el-option v-for="r in resources" :key="r.id" :value="r.id" :label="r.name" />
          </el-select>
        </div>
      </template>
      <div ref="chartRef" class="forecast-chart"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { Cpu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { triggerForecast } from '@/api/forecast'
import { getForecast } from '@/api/dashboard'
import { getResourcePage } from '@/api/resource'
import type { ResourceVO, ForecastPointVO } from '@/types'
import { formatDateTime } from '@/utils/format'

const triggerLoading = ref(false)
const chartLoading = ref(false)
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const resources = ref<ResourceVO[]>([])
const selectedResource = ref<number | undefined>(undefined)

async function handleTrigger() {
  triggerLoading.value = true
  try {
    await triggerForecast()
    ElMessage.success('预测任务已触发')
    fetchForecast()
  } finally {
    triggerLoading.value = false
  }
}

async function fetchForecast() {
  chartLoading.value = true
  try {
    const res = await getForecast(selectedResource.value, 24)
    renderChart(res.data)
  } finally {
    chartLoading.value = false
  }
}

function renderChart(data: ForecastPointVO[]) {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const grouped: Record<number, ForecastPointVO[]> = {}
  data.forEach((p) => {
    if (!grouped[p.resourceId]) grouped[p.resourceId] = []
    grouped[p.resourceId].push(p)
  })
  const times = data.length > 0 ? [...new Set(data.map((p) => formatDateTime(p.forecastTime)))] : []
  const series = Object.entries(grouped).map(([rid, points]) => {
    const r = resources.value.find((x) => x.id === Number(rid))
    return {
      name: r?.name || `资源${rid}`,
      type: 'line',
      smooth: true,
      data: times.map((t) => {
        const p = points.find((x) => formatDateTime(x.forecastTime) === t)
        return p?.heatValue ?? 0
      }),
    }
  })
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { top: 40, left: 50, right: 20, bottom: 30 },
    xAxis: { type: 'category', data: times, axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', name: '热度', max: 100 },
    series,
  })
}

async function initResources() {
  const res = await getResourcePage({ page: 1, size: 50 })
  resources.value = res.data.records
}

onMounted(async () => {
  await initResources()
  await nextTick()
  fetchForecast()
})

onUnmounted(() => { chart?.dispose() })
</script>

<style scoped lang="scss">
.trigger-card { margin-bottom: 20px; }
.trigger-content {
  display: flex; align-items: center; gap: 20px;
  .trigger-icon { font-size: 48px; }
  .trigger-info { flex: 1; h3 { font-size: 18px; margin-bottom: 6px; } p { font-size: 13px; color: #86909c; } }
}
.forecast-card {
  .card-header { display: flex; justify-content: space-between; align-items: center; }
  .forecast-chart { height: 400px; }
}
</style>