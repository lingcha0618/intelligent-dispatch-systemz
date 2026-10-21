<template>
  <div class="page-container">
    <h2 class="page-title">参数配置</h2>

    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>系统参数</span>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存配置</el-button>
        </div>
      </template>
      <el-table :data="configList" stripe>
        <el-table-column label="参数名" min-width="240">
          <template #default="{ row }">
            <div class="param-name">
              <span class="param-label">{{ configNameMap[row.key] || row.key }}</span>
              <span class="param-key">{{ row.key }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="参数值" min-width="200">
          <template #default="{ row }">
            <el-input v-model="row.value" size="small" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllConfig, updateConfig } from '@/api/config'
import type { ConfigItemDTO } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const configList = ref<ConfigItemDTO[]>([])

const configNameMap: Record<string, string> = {
  'booking.max_daily_per_user': '每人每日最大预约次数',
  'booking.max_duration_hours': '单次最长预约小时数',
  'booking.overtime_minutes': '开始后多少分钟未签到判违约',
  'booking.remind_minutes': '预约到期前多少分钟提醒',
  'booking.ban_threshold': '累计违约几次封禁',
  'booking.ban_days': '封禁天数',
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAllConfig()
    configList.value = Object.entries(res.data).map(([key, value]) => ({ key, value }))
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await updateConfig(configList.value)
    ElMessage.success('配置保存成功')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.param-name {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
  .param-label { color: #1d2129; }
  .param-key { font-size: 12px; color: #a9aeb8; }
}
</style>