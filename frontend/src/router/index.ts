import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layout/StudentLayout.vue'),
    redirect: '/resources',
    children: [
      {
        path: 'resources',
        name: 'ResourceList',
        component: () => import('@/views/student/ResourceList.vue'),
        meta: { title: '资源浏览' },
      },
      {
        path: 'resources/:id',
        name: 'ResourceDetail',
        component: () => import('@/views/student/ResourceDetail.vue'),
        meta: { title: '资源详情' },
      },
      {
        path: 'my-bookings',
        name: 'MyBookings',
        component: () => import('@/views/student/MyBookings.vue'),
        meta: { title: '我的预约' },
      },
      {
        path: 'notices',
        name: 'StudentNotices',
        component: () => import('@/views/student/Notices.vue'),
        meta: { title: '消息通知' },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/resources',
    children: [
      {
        path: 'resources',
        name: 'AdminResources',
        component: () => import('@/views/admin/ResourceManage.vue'),
        meta: { title: '资源管理', requireAdmin: true },
      },
      {
        path: 'bookings',
        name: 'AdminBookings',
        component: () => import('@/views/admin/BookingManage.vue'),
        meta: { title: '预约管理', requireAdmin: true },
      },
      {
        path: 'violations',
        name: 'AdminViolations',
        component: () => import('@/views/admin/ViolationManage.vue'),
        meta: { title: '违约管理', requireAdmin: true },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', requireAdmin: true },
      },
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理', requireAdmin: true },
      },
      {
        path: 'config',
        name: 'AdminConfig',
        component: () => import('@/views/admin/ConfigManage.vue'),
        meta: { title: '参数配置', requireAdmin: true },
      },
      {
        path: 'forecast',
        name: 'AdminForecast',
        component: () => import('@/views/admin/ForecastTrigger.vue'),
        meta: { title: 'AI预测', requireAdmin: true },
      },
    ],
  },
  {
    path: '/dashboard',
    name: 'DataScreen',
    component: () => import('@/views/dashboard/DataScreen.vue'),
    meta: { title: '数据大屏', requireAdmin: true },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  document.title = `${to.meta.title || ''} - 校园资源智能预约调度系统`

  if (to.path === '/login') {
    if (auth.isLogin) {
      next(auth.isAdmin ? '/admin' : '/')
    } else {
      next()
    }
    return
  }

  if (!auth.isLogin) {
    next('/login')
    return
  }

  if (to.meta.requireAdmin && !auth.isAdmin) {
    next('/')
    return
  }

  if (to.path.startsWith('/admin') && !auth.isAdmin) {
    next('/')
    return
  }

  next()
})

export default router