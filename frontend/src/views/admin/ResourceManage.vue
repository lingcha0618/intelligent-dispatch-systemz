<template>
  <div class="page-container">
    <div class="header-row">
      <h2 class="page-title">资源管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        新增资源
      </el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="filter.type" placeholder="资源类型" clearable @change="handleSearch" style="width: 140px">
        <el-option :value="1" label="教室" />
        <el-option :value="2" label="图书馆座位" />
        <el-option :value="3" label="实验室" />
        <el-option :value="4" label="会议室" />
      </el-select>
      <el-select v-model="filter.status" placeholder="状态" clearable @change="handleSearch" style="width: 120px">
        <el-option :value="0" label="空闲" />
        <el-option :value="1" label="占用" />
        <el-option :value="2" label="维护中" />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="编号" prop="id" width="70" />
      <el-table-column label="名称" prop="name" min-width="140" />
      <el-table-column label="类型" prop="typeDesc" width="120" />
      <el-table-column label="位置" prop="location" width="140" />
      <el-table-column label="容量" prop="capacity" width="80" />
      <el-table-column label="开放时间" width="140">
        <template #default="{ row }">{{ formatLocalTime(row.openStart) }} - {{ formatLocalTime(row.openEnd) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="light">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="热度" width="100">
        <template #default="{ row }">
          <el-progress :percentage="row.heatValue ?? 0" :show-text="false" :stroke-width="6" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row as ResourceVO)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row as ResourceVO)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑资源' : '新增资源'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option :value="1" label="教室" />
            <el-option :value="2" label="图书馆座位" />
            <el-option :value="3" label="实验室" />
            <el-option :value="4" label="会议室" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="容量" required>
          <el-input-number v-model="form.capacity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="开放开始">
          <el-time-picker v-model="openStartVal" format="HH:mm" value-format="HH:mm" placeholder="如 08:00" />
        </el-form-item>
        <el-form-item label="开放结束">
          <el-time-picker v-model="openEndVal" format="HH:mm" value-format="HH:mm" placeholder="如 22:00" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="0" label="空闲" />
            <el-option :value="1" label="占用" />
            <el-option :value="2" label="维护中" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResourcePage, createResource, updateResource, deleteResource } from '@/api/resource'
import type { ResourceVO, ResourceDTO } from '@/types'
import { formatLocalTime } from '@/utils/format'

const loading = ref(false)
const list = ref<ResourceVO[]>([])
const total = ref(0)
const filter = reactive({ page: 1, size: 10, type: undefined as number | undefined, status: undefined as number | undefined })

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const submitLoading = ref(false)
const openStartVal = ref<string>('')
const openEndVal = ref<string>('')

const form = reactive<ResourceDTO>({
  name: '', type: 1, location: '', capacity: 10, status: 0, description: '',
})

function statusType(s: number): 'success' | 'warning' | 'info' {
  return s === 0 ? 'success' : s === 1 ? 'warning' : 'info'
}

function handleSearch() { filter.page = 1; fetchData() }

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

function openDialog(row?: ResourceVO) {
  if (row) {
    editId.value = row.id
    Object.assign(form, { name: row.name, type: row.type, location: row.location, capacity: row.capacity, status: row.status, description: row.description })
    openStartVal.value = row.openStart ? formatLocalTime(row.openStart) : ''
    openEndVal.value = row.openEnd ? formatLocalTime(row.openEnd) : ''
  } else {
    editId.value = null
    Object.assign(form, { name: '', type: 1, location: '', capacity: 10, status: 0, description: '' })
    openStartVal.value = ''
    openEndVal.value = ''
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name || !form.capacity) { ElMessage.warning('请填写必填项'); return }
  submitLoading.value = true
  try {
    const payload = { ...form, openStart: openStartVal.value || undefined, openEnd: openEndVal.value || undefined }
    if (editId.value) {
      await updateResource(editId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createResource(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: ResourceVO) {
  await ElMessageBox.confirm(`确认删除资源「${row.name}」？`, '删除确认', { type: 'warning' })
  await deleteResource(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; .page-title { margin-bottom: 0; } }
.pagination-bar { display: flex; justify-content: center; margin-top: 20px; }
</style>