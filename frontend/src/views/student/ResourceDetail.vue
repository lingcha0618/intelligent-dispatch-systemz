<template>
  <div class="page-container">
    <el-page-header @back="router.back()" class="page-back">
      <template #content>
        <span class="page-title">资源详情</span>
      </template>
    </el-page-header>

    <div v-loading="loading" class="detail-content" v-if="detail">
      <div class="detail-card">
        <div class="detail-header">
          <span class="type-icon">{{ ResourceTypeIcon[detail.type] }}</span>
          <div class="header-info">
            <h2>{{ detail.name }}</h2>
            <el-tag :type="statusTagType(detail.status)" effect="light">{{ detail.statusDesc }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型">{{ detail.typeDesc }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ detail.location || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="容纳人数">{{ detail.capacity }} 人</el-descriptions-item>
          <el-descriptions-item label="开放时间">
            {{ formatLocalTime(detail.openStart) }} - {{ formatLocalTime(detail.openEnd) }}
          </el-descriptions-item>
          <el-descriptions-item label="热度值" :span="2">
            <div class="heat-inline">
              <el-progress :percentage="detail.heatValue ?? 0" :color="heatColor(detail.heatValue)" :stroke-width="10" />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detail.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="slots-card">
        <h3 class="card-title">可预约时段</h3>
        <div class="slots-grid" v-if="detail.slots && detail.slots.length > 0">
          <div
            v-for="(slot, idx) in detail.slots"
            :key="idx"
            class="slot-item"
            :class="{ bookable: slot.bookable, selected: selectedSlot === idx }"
            @click="slot.bookable && selectSlot(idx)"
          >
            <div class="slot-time">{{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}</div>
            <div class="slot-status">
              <el-tag v-if="slot.bookable" type="success" size="small" effect="plain">可预约</el-tag>
              <el-tag v-else type="info" size="small" effect="plain">已满</el-tag>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无可预约时段" />

        <div class="booking-action" v-if="selectedSlot !== null">
          <div class="selected-info">
            已选时段：<strong>{{ formatTime(detail.slots[selectedSlot].startTime) }} - {{ formatTime(detail.slots[selectedSlot].endTime) }}</strong>
          </div>
          <el-button type="primary" size="large" :loading="bookingLoading" @click="handleBooking">
            确认预约
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResourceDetail } from '@/api/resource'
import { getIdempotentToken, createBooking } from '@/api/booking'
import { ResourceTypeIcon } from '@/types'
import type { ResourceDetailVO } from '@/types'
import { formatLocalTime, formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const bookingLoading = ref(false)
const detail = ref<ResourceDetailVO | null>(null)
const selectedSlot = ref<number | null>(null)

function statusTagType(status: number) {
  if (status === 0) return 'success'
  if (status === 1) return 'warning'
  return 'info'
}

function heatColor(val: number | null) {
  if (val == null) return '#c9cdd4'
  if (val >= 70) return '#f53f3f'
  if (val >= 40) return '#ff9a2e'
  return '#4facfe'
}

function selectSlot(idx: number) {
  selectedSlot.value = idx
}

async function fetchData() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getResourceDetail(id)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleBooking() {
  if (!detail.value || selectedSlot.value === null) return
  const slot = detail.value.slots[selectedSlot.value]
  bookingLoading.value = true
  try {
    const tokenRes = await getIdempotentToken()
    const res = await createBooking({
      requestId: tokenRes.data.requestId,
      resourceId: detail.value.id,
      startTime: slot.startTime,
      endTime: slot.endTime,
    })
    await ElMessageBox.alert(
      `预约成功！\n资源：${res.data.resourceName}\n时段：${formatTime(res.data.startTime)} - ${formatTime(res.data.endTime)}`,
      '预约确认',
      { confirmButtonText: '查看我的预约', type: 'success' },
    )
    router.push('/my-bookings')
  } catch (e) {
    if (e instanceof Error && e.message) {
      // already handled by interceptor
    }
  } finally {
    bookingLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-back {
  margin-bottom: 20px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card, .slots-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e6eb;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  .type-icon {
    font-size: 48px;
  }

  .header-info {
    display: flex;
    flex-direction: column;
    gap: 8px;

    h2 {
      font-size: 22px;
      font-weight: 700;
    }
  }
}

.heat-inline {
  width: 100%;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.slot-item {
  border: 2px solid #e5e6eb;
  border-radius: 10px;
  padding: 16px;
  text-align: center;
  cursor: not-allowed;
  transition: all 0.2s;
  opacity: 0.5;

  &.bookable {
    opacity: 1;
    cursor: pointer;

    &:hover {
      border-color: #4facfe;
      background: #f0f9ff;
    }
  }

  &.selected {
    border-color: #4facfe;
    background: #e8f4ff;
    box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.15);
  }

  .slot-time {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 8px;
  }
}

.booking-action {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .selected-info {
    font-size: 15px;
    color: #1d2129;

    strong {
      color: #4facfe;
    }
  }
}
</style>