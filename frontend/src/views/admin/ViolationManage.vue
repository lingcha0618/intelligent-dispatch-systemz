<template>
  <div class="page-container">
    <div class="header-row">
      <h2 class="page-title">违约管理</h2>
      <el-button type="primary" @click="dialogVisible = true">
        <el-icon><Plus /></el-icon>
        登记违约
      </el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="filter.type" placeholder="违约类型" clearable @change="handleSearch" style="width: 150px">
        <el-option :value="1" label="超时未签到" />
        <el-option :value="2" label="早退" />
        <el-option :value="3" label="恶意占座" />
      </el-select>
      <el-input v-model="filter.userId" placeholder="用户编号" clearable @change="handleSearch" style="width: 120px" />
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="编号" prop="id" width="70" />
      <el-table-column label="用户编号" prop="userId" width="90" />
      <el-table-column label="预约编号" prop="bookingId" width="90" />
      <el-table-column label="违约类型" width="130">
        <template #default="{ row }">
          <el-tag :type="violationTypeColor(row.type)" effect="light">{{ row.typeDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="登记时间" width="180">
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

    <el-dialog v-model="dialogVisible" title="登记违约" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="预约编号" required>
          <el-input-number v-model="form.bookingId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="违约类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option :value="1" label="超时未签到" />
            <el-option :value="2" label="早退" />
            <el-option :value="3" label="恶意占座" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getViolationPage, createViolation } from '@/api/violation'
import type { ViolationVO } from '@/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref<ViolationVO[]>([])
const total = ref(0)
const filter = reactive({ page: 1, size: 10, type: undefined as number | undefined, userId: undefined as number | undefined })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const form = reactive({ bookingId: 1, type: 1 })

function violationTypeColor(t: number) {
  return t === 1 ? 'warning' : t === 2 ? 'info' : 'danger'
}

function handleSearch() { filter.page = 1; fetchData() }

async function fetchData() {
  loading.value = true
  try {
    const res = await getViolationPage({ ...filter, userId: filter.userId || undefined })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await createViolation({ ...form })
    ElMessage.success('违约登记成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; .page-title { margin-bottom: 0; } }
.pagination-bar { display: flex; justify-content: center; margin-top: 20px; }
</style>