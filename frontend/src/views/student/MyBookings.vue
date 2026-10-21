<template>
  <div class="page-container">
    <h2 class="page-title">我的预约</h2>

    <div class="filter-bar">
      <el-radio-group v-model="filter.status" @change="handleSearch">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">待签到</el-radio-button>
        <el-radio-button :value="1">使用中</el-radio-button>
        <el-radio-button :value="2">已完成</el-radio-button>
        <el-radio-button :value="3">已取消</el-radio-button>
        <el-radio-button :value="4">违约</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list" v-loading="loading" stripe class="booking-table">
      <el-table-column label="资源" min-width="160">
        <template #default="{ row }">
          <div class="resource-cell">
            <span class="type-icon">{{ ResourceTypeIcon[row.resourceType] }}</span>
            <div>
              <div class="res-name">{{ row.resourceName }}</div>
              <div class="res-type">{{ row.resourceTypeDesc }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="bookingStatusType(row.status)" effect="light">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="handleCheckin(row as BookingVO)">签到</el-button>
          <el-button v-if="row.status === 0" type="danger" size="small" plain @click="handleCancel(row as BookingVO)">取消</el-button>
          <span v-if="row.status !== 0" class="no-action">-</span>
        </template>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBookings, checkinBooking, cancelBooking } from '@/api/booking'
import { ResourceTypeIcon } from '@/types'
import type { BookingVO } from '@/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref<BookingVO[]>([])
const total = ref(0)

const filter = reactive({
  page: 1,
  size: 10,
  status: undefined as number | undefined,
})

function bookingStatusType(status: number): 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, 'success' | 'warning' | 'info' | 'danger'> = { 0: 'warning', 1: 'success', 2: 'info', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

function handleSearch() {
  filter.page = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyBookings(filter)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleCheckin(row: BookingVO) {
  await ElMessageBox.confirm(`确认签到「${row.resourceName}」？`, '签到确认', { type: 'info' })
  await checkinBooking(row.id)
  ElMessage.success('签到成功')
  fetchData()
}

async function handleCancel(row: BookingVO) {
  await ElMessageBox.confirm(`确认取消预约「${row.resourceName}」？`, '取消确认', { type: 'warning' })
  await cancelBooking(row.id)
  ElMessage.success('已取消预约')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.booking-table {
  border-radius: 8px;
  overflow: hidden;
}

.resource-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  .type-icon {
    font-size: 24px;
  }

  .res-name {
    font-weight: 600;
    font-size: 14px;
  }

  .res-type {
    font-size: 12px;
    color: #86909c;
  }
}

.no-action {
  color: #c9cdd4;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>