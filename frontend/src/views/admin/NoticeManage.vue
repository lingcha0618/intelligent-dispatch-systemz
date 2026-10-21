<template>
  <div class="page-container">
    <h2 class="page-title">公告管理</h2>

    <el-card class="announce-card">
      <template #header>
        <span>发布系统公告</span>
      </template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入公告内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            <el-icon><Promotion /></el-icon>
            发送公告
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="history-card">
      <template #header>
        <span>历史公告</span>
      </template>
      <div v-loading="loading" class="notice-list">
        <div v-for="item in list" :key="item.id" class="notice-item">
          <div class="notice-icon">📢</div>
          <div class="notice-body">
            <div class="notice-title">{{ item.title }}</div>
            <p class="notice-content">{{ item.content }}</p>
            <span class="notice-time">{{ formatDateTime(item.createTime) }}</span>
          </div>
        </div>
        <el-empty v-if="!loading && list.length === 0" description="暂无公告" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { sendAnnouncement, getNoticePage } from '@/api/notice'
import type { NoticeVO } from '@/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const list = ref<NoticeVO[]>([])
const form = reactive({ title: '', content: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getNoticePage({ page: 1, size: 20 })
    list.value = res.data.records.filter((n) => n.type === 4)
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.title || !form.content) { ElMessage.warning('请填写标题和内容'); return }
  submitLoading.value = true
  try {
    await sendAnnouncement({ ...form })
    ElMessage.success('公告发送成功')
    form.title = ''
    form.content = ''
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.announce-card { margin-bottom: 20px; }
.notice-list { display: flex; flex-direction: column; gap: 12px; }
.notice-item {
  display: flex; gap: 12px; padding: 12px; border-radius: 8px; background: #f9fafb;
  .notice-icon { font-size: 20px; }
  .notice-title { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
  .notice-content { font-size: 13px; color: #4e5969; line-height: 1.5; margin-bottom: 4px; }
  .notice-time { font-size: 12px; color: #a9aeb8; }
}
</style>