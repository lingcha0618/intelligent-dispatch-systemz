<template>
  <div class="page-container">
    <div class="header-row">
      <h2 class="page-title">消息通知</h2>
      <el-button type="primary" plain @click="handleReadAll" :disabled="!hasUnread">
        <el-icon><Check /></el-icon>
        全部已读
      </el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filter.isRead" @change="handleSearch">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">未读</el-radio-button>
        <el-radio-button :value="1">已读</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="notice-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="notice-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleRead(item)"
      >
        <div class="notice-icon" :class="noticeTypeClass(item.type)">
          {{ noticeTypeIcon(item.type) }}
        </div>
        <div class="notice-body">
          <div class="notice-header">
            <span class="notice-title">{{ item.title }}</span>
            <el-tag size="small" :type="noticeTagType(item.type)" effect="plain">{{ item.typeDesc }}</el-tag>
            <span v-if="item.isRead === 0" class="unread-dot"></span>
          </div>
          <p class="notice-content">{{ item.content }}</p>
          <span class="notice-time">{{ formatDateTime(item.createTime) }}</span>
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无通知" />
    </div>

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
import { ref, reactive, computed, onMounted } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNoticePage, readNotice, readAllNotices } from '@/api/notice'
import type { NoticeVO } from '@/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref<NoticeVO[]>([])
const total = ref(0)

const filter = reactive({
  page: 1,
  size: 10,
  isRead: undefined as number | undefined,
})

const hasUnread = computed(() => list.value.some((n) => n.isRead === 0))

function noticeTypeIcon(type: number) {
  const icons: Record<number, string> = { 1: '✅', 2: '⏰', 3: '⚠️', 4: '📢' }
  return icons[type] || '📬'
}

function noticeTypeClass(type: number) {
  const classes: Record<number, string> = { 1: 'type-success', 2: 'type-warning', 3: 'type-danger', 4: 'type-info' }
  return classes[type] || ''
}

function noticeTagType(type: number) {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
  return map[type] || 'info'
}

function handleSearch() {
  filter.page = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getNoticePage(filter)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleRead(item: NoticeVO) {
  if (item.isRead === 1) return
  await readNotice(item.id)
  item.isRead = 1
}

async function handleReadAll() {
  await readAllNotices()
  ElMessage.success('已全部标记为已读')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .page-title {
    margin-bottom: 0;
  }
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  gap: 16px;
  border: 1px solid #e5e6eb;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #4facfe;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  &.unread {
    border-left: 4px solid #4facfe;
  }

  .notice-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;
    background: #f2f3f5;

    &.type-success { background: #e8ffea; }
    &.type-warning { background: #fff7e8; }
    &.type-danger { background: #ffece8; }
    &.type-info { background: #e8f4ff; }
  }

  .notice-body {
    flex: 1;
  }

  .notice-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;

    .notice-title {
      font-size: 15px;
      font-weight: 600;
      color: #1d2129;
    }

    .unread-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #f53f3f;
    }
  }

  .notice-content {
    font-size: 13px;
    color: #4e5969;
    line-height: 1.6;
    margin-bottom: 6px;
  }

  .notice-time {
    font-size: 12px;
    color: #a9aeb8;
  }
}

.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>