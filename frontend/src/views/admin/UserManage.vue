<template>
  <div class="page-container">
    <div class="header-row">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" @click="dialogVisible = true">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="filter.keyword" placeholder="搜索用户名/姓名" clearable @change="handleSearch" style="width: 200px" />
      <el-select v-model="filter.role" placeholder="角色" clearable @change="handleSearch" style="width: 130px">
        <el-option :value="0" label="学生" />
        <el-option :value="1" label="管理员" />
        <el-option :value="2" label="超级管理员" />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="编号" prop="id" width="70" />
      <el-table-column label="用户名" prop="username" min-width="120" />
      <el-table-column label="真实姓名" prop="realName" width="120" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="row.role === 2 ? 'danger' : row.role === 1 ? 'warning' : 'info'" effect="plain">{{ row.roleDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="手机号" prop="phone" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" effect="light">{{ row.status === 0 ? '正常' : '封禁' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="封禁截止" width="160">
        <template #default="{ row }">{{ formatDateTime(row.banUntil) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="danger" @click="handleBan(row)">封禁</el-button>
          <el-button v-else size="small" type="success" @click="handleUnban(row)">解封</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增用户" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width: 100%">
            <el-option :value="0" label="学生" />
            <el-option :value="1" label="管理员" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
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
import { getUserPage, createUser, updateUserStatus } from '@/api/user'
import type { UserVO, UserCreateDTO } from '@/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref<UserVO[]>([])
const total = ref(0)
const filter = reactive({ page: 1, size: 10, keyword: '', role: undefined as number | undefined })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const form = reactive<UserCreateDTO>({ username: '', realName: '', password: '', role: 0, phone: '' })

function handleSearch() { filter.page = 1; fetchData() }

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage({ ...filter, keyword: filter.keyword || undefined })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.username || !form.realName || !form.password) { ElMessage.warning('请填写必填项'); return }
  submitLoading.value = true
  try {
    await createUser({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    Object.assign(form, { username: '', realName: '', password: '', role: 0, phone: '' })
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleBan(row: UserVO) {
  const { value } = await ElMessageBox.prompt('请输入封禁截止时间（留空则永久封禁）', `封禁「${row.realName}」`, {
    confirmButtonText: '确认封禁',
    cancelButtonText: '取消',
    inputPlaceholder: '如 2026-12-31T23:59:59',
  })
  await updateUserStatus(row.id, { status: 1, banUntil: value || undefined })
  ElMessage.success('已封禁')
  fetchData()
}

async function handleUnban(row: UserVO) {
  await ElMessageBox.confirm(`确认解封「${row.realName}」？`, '解封确认', { type: 'info' })
  await updateUserStatus(row.id, { status: 0 })
  ElMessage.success('已解封')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; .page-title { margin-bottom: 0; } }
.pagination-bar { display: flex; justify-content: center; margin-top: 20px; }
</style>