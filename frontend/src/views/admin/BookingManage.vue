<template>
  <div class="page-container">
    <h2 class="page-title">预约管理</h2>

    <div class="filter-bar">
      <el-select v-model="filter.status" placeholder="预约状态" clearable @change="handleSearch" style="width: 130px">
        <el-option :value="0" label="待签到" />
        <el-option :value="1" label="使用中" />
        <el-option :value="2" label="已完成" />
        <el-option :value="3" label="已取消" />
        <el-option :value="4" label="违约" />
      </el-select>
      <el-input v-model="filter.resourceId" placeholder="资源编号" clearable @change="handleSearch" style="width: 120px" />
      <el-input v-model="filter.userId" placeholder="用户编号" clearable @change="handleSearch" style="width: 120px" />
      <el-button type="primary" plain @click="handleExport">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="编号" prop="id" width="70" />
      <el-table-column label="用户编号" prop="userId" width="80" />
      <el-table-column label="资源" prop="resourceName" min-width="140" />
      <el-table-column label="类型" prop="resourceTypeDesc" width="110" />
      <el-table-column label="开始时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="light">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="filter.page"
        v-model:page-size="filter.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getBookingPage } from '@/api/booking'
import { exportBookings } from '@/api/export'
import type { BookingVO } from '@/types'
import { formatDateTime, downloadBlob } from '@/utils/format'

const loading = ref(false)
const list = ref<BookingVO[]>([])
const total = ref(0)
const filter = reactive({
  page: 1, size: 10,
  status: undefined as number | undefined,
  resourceId: undefined as number | undefined,
  userId: undefined as number | undefined,
})

function statusType(s: number): 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, 'success' | 'warning' | 'info' | 'danger'> = { 0: 'warning', 1: 'success', 2: 'info', 3: 'info', 4: 'danger' }
  return map[s] || 'info'
}

function handleSearch() { filter.page = 1; fetchData() }

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: filter.page, size: filter.size,
      status: filter.status,
      resourceId: filter.resourceId || undefined,
      userId: filter.userId || undefined,
    }
    const res = await getBookingPage(params)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  try {
    const res = await exportBookings({ format: 'csv' })
    downloadBlob(res as unknown as Blob, `bookings_${Date.now()}.csv`)
    ElMessage.success('导出成功')
  } catch {
    // handled
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.pagination-bar { display: flex; justify-content: center; margin-top: 20px; }
</style>