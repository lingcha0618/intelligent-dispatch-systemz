<template>
  <el-container class="admin-layout">
    <el-aside width="230px" class="sidebar">
      <div class="sidebar-logo">
        <span class="logo-icon">⚙️</span>
        <span class="logo-text">管理后台</span>
      </div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu" :default-openeds="['/admin']">
        <el-menu-item index="/admin/resources">
          <el-icon><Box /></el-icon>
          <span>资源管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/bookings">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/violations">
          <el-icon><Warning /></el-icon>
          <span>违约管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/notices">
          <el-icon><Promotion /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/config">
          <el-icon><Setting /></el-icon>
          <span>参数配置</span>
        </el-menu-item>
        <el-menu-item index="/admin/forecast">
          <el-icon><DataLine /></el-icon>
          <span>AI 预测</span>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>数据大屏</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="top-header">
        <div class="header-left">
          <span class="page-breadcrumb">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-button text @click="router.push('/')">
            <el-icon><Switch /></el-icon>
            切换到用户端
          </el-button>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">{{ auth.realName?.charAt(0) }}</el-avatar>
              <span class="user-name">{{ auth.realName }}</span>
              <el-tag size="small" type="danger" effect="plain" class="role-tag">
                {{ auth.role === 'SUPER_ADMIN' ? '超管' : '管理员' }}
              </el-tag>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Box, Calendar, Warning, UserFilled, Promotion, Setting, DataLine, Monitor, ArrowDown, Switch } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => (route.meta.title as string) || '')

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}

.sidebar {
  background: #1d2129;
  display: flex;
  flex-direction: column;

  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 20px;
    border-bottom: 1px solid #2b2f36;

    .logo-icon {
      font-size: 24px;
    }

    .logo-text {
      font-size: 18px;
      font-weight: 700;
      color: #fff;
    }
  }

  .sidebar-menu {
    border-right: none;
    flex: 1;
    background: #1d2129;

    :deep(.el-menu-item) {
      color: #c9cdd4;

      &:hover {
        background: #2b2f36;
        color: #fff;
      }

      &.is-active {
        background: #4facfe;
        color: #fff;
      }
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
    gap: 16px;
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
      background: #1d2129;
      color: #fff;
      font-weight: 600;
    }

    .user-name {
      font-size: 14px;
      color: #1d2129;
    }

    .role-tag {
      margin-left: 2px;
    }
  }
}

.main-content {
  background: #f5f6f7;
  padding: 0;
  overflow-y: auto;
}
</style>