<template>
  <el-container class="student-layout">
    <el-aside width="220px" class="sidebar">
      <div class="sidebar-logo">
        <span class="logo-icon">🏫</span>
        <span class="logo-text">校园预约</span>
      </div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu">
        <el-menu-item index="/resources">
          <el-icon><Grid /></el-icon>
          <span>资源浏览</span>
        </el-menu-item>
        <el-menu-item index="/my-bookings">
          <el-icon><Calendar /></el-icon>
          <span>我的预约</span>
        </el-menu-item>
        <el-menu-item index="/notices">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="menu-badge" />
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="top-header">
        <div class="header-left">
          <span class="page-breadcrumb">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-button v-if="auth.isAdmin" text @click="router.push('/admin')">
            <el-icon><Switch /></el-icon>
            切换到管理后台
          </el-button>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">{{ auth.realName?.charAt(0) }}</el-avatar>
              <span class="user-name">{{ auth.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Grid, Calendar, Bell, ArrowDown, Switch } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getNoticePage } from '@/api/notice'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => (route.meta.title as string) || '')
const unreadCount = ref(0)

async function fetchUnread() {
  try {
    const res = await getNoticePage({ page: 1, size: 1, isRead: 0 })
    unreadCount.value = res.data.total
  } catch {
    // ignore
  }
}

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  }
}

onMounted(fetchUnread)
</script>

<style scoped lang="scss">
.student-layout {
  height: 100vh;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e5e6eb;
  display: flex;
  flex-direction: column;

  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 20px;
    border-bottom: 1px solid #e5e6eb;

    .logo-icon {
      font-size: 28px;
    }

    .logo-text {
      font-size: 18px;
      font-weight: 700;
      color: #1d2129;
    }
  }

  .sidebar-menu {
    border-right: none;
    flex: 1;

    .menu-badge {
      margin-left: auto;
    }
  }
}

.top-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;

  .page-breadcrumb {
    font-size: 16px;
    font-weight: 600;
    color: #1d2129;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 12px;
    border-radius: 8px;
    transition: background 0.2s;

    &:hover {
      background: #f2f3f5;
    }

    .user-avatar {
      background: #4facfe;
      color: #fff;
      font-weight: 600;
    }

    .user-name {
      font-size: 14px;
      color: #1d2129;
    }
  }
}

.main-content {
  background: #f5f6f7;
  padding: 0;
  overflow-y: auto;
}
</style>