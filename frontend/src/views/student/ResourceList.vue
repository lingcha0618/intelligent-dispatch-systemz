<template>
  <div class="page-container">
    <h2 class="page-title">资源浏览</h2>

    <div class="filter-bar">
      <el-radio-group v-model="filter.type" @change="handleSearch">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="1">🏫 教室</el-radio-button>
        <el-radio-button :value="2">📚 图书馆</el-radio-button>
        <el-radio-button :value="3">🔬 实验室</el-radio-button>
        <el-radio-button :value="4">🏢 会议室</el-radio-button>
      </el-radio-group>
      <el-radio-group v-model="filter.status" @change="handleSearch">
        <el-radio-button :value="undefined">全部状态</el-radio-button>
        <el-radio-button :value="0">空闲</el-radio-button>
        <el-radio-button :value="1">占用</el-radio-button>
        <el-radio-button :value="2">维护中</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="resource-grid">
      <div
        v-for="item in list"
        :key="item.id"
        class="resource-card"
        :class="{ 'status-maintenance': item.status === 2 }"
        @click="goDetail(item.id)"
      >
        <div class="card-header">
          <span class="type-icon">{{ ResourceTypeIcon[item.type] }}</span>
          <el-tag :type="statusTagType(item.status)" size="small" effect="light">
            {{ item.statusDesc }}
          </el-tag>
        </div>
        <h3 class="card-name">{{ item.name }}</h3>
        <div class="card-meta">
          <span class="meta-item">
            <el-icon><Location /></el-icon>
            {{ item.location || '未设置' }}
          </span>
          <span class="meta-item">
            <el-icon><User /></el-icon>
            容纳 {{ item.capacity }} 人
          </span>
        </div>
        <div class="heat-bar">
          <div class="heat-label">
            <span>热度</span>
            <span class="heat-value">{{ item.heatValue ?? '-' }}</span>
          </div>
          <el-progress :percentage="item.heatValue ?? 0" :show-text="false" :stroke-width="6" :color="heatColor(item.heatValue)" />
        </div>
        <div class="card-footer">
          <span class="type-desc">{{ item.typeDesc }}</span>
          <el-button type="primary" text size="small">
            查看详情
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无资源" class="grid-empty" />
    </div>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="filter.page"
        v-model:page-size="filter.size"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location, User, ArrowRight } from '@element-plus/icons-vue'
import { getResourcePage } from '@/api/resource'
import { ResourceTypeIcon } from '@/types'
import type { ResourceVO } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<ResourceVO[]>([])
const total = ref(0)

const filter = reactive({
  page: 1,
  size: 12,
  type: undefined as number | undefined,
  status: undefined as number | undefined,
})

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

function handleSearch() {
  filter.page = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getResourcePage(filter)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/resources/${id}`)
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.resource-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid #e5e6eb;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border-color: #4facfe;
  }

  &.status-maintenance {
    opacity: 0.7;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .type-icon {
      font-size: 32px;
    }
  }

  .card-name {
    font-size: 17px;
    font-weight: 600;
    color: #1d2129;
  }

  .card-meta {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #86909c;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .heat-bar {
    .heat-label {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #86909c;
      margin-bottom: 4px;

      .heat-value {
        font-weight: 600;
        color: #1d2129;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 8px;
    border-top: 1px solid #f2f3f5;

    .type-desc {
      font-size: 12px;
      color: #86909c;
    }
  }
}

.grid-empty {
  grid-column: 1 / -1;
  padding: 60px 0;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>