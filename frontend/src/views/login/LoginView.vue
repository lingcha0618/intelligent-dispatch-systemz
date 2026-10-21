<template>
  <div class="login-page">
    <div class="login-visual">
      <div class="visual-bg">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
      </div>
      <div class="visual-content">
        <div class="brand-icon">🏫</div>
        <h1 class="brand-title">校园资源智能<br />预约调度系统</h1>
        <p class="brand-subtitle">教室 · 图书馆座位 · 实验室 · 会议室</p>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>实时时段预约，防超卖保障</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>自动违约检测与信用管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>AI 热度预测，智能推荐</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>数据大屏，全局态势一目了然</span>
          </div>
        </div>
      </div>
    </div>

    <div class="login-form-side">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>{{ activeTab === 'login' ? '欢迎回来' : '注册账号' }}</h2>
          <p>{{ activeTab === 'login' ? '登录以开始预约校园资源' : '填写信息完成学生注册' }}</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large" @submit.prevent="handleLogin">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="学号 / 工号" :prefix-icon="User" clearable />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loginLoading" @click="handleLogin">
                登 录
              </el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large" @submit.prevent="handleRegister">
              <el-form-item prop="username">
                <el-input v-model="registerForm.username" placeholder="学号 / 工号" :prefix-icon="User" clearable />
              </el-form-item>
              <el-form-item prop="realName">
                <el-input v-model="registerForm.realName" placeholder="真实姓名" :prefix-icon="Postcard" clearable />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input v-model="registerForm.phone" placeholder="手机号（选填）" :prefix-icon="Iphone" clearable />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="设置密码" :prefix-icon="Lock" show-password />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="registerLoading" @click="handleRegister">
                注 册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="form-footer">
          <span>校园资源智能预约调度系统 © 2026</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Iphone, Postcard } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const auth = useAuthStore()

const activeTab = ref<'login' | 'register'>('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', realName: '', phone: '', password: '' })

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入学号/工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules: FormRules = {
  username: [{ required: true, message: '请输入学号/工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loginLoading.value = true
  try {
    const res = await loginApi(loginForm)
    auth.setAuth(res.data)
    ElMessage.success(`欢迎回来，${res.data.realName}`)
    router.push(auth.isAdmin ? '/admin' : '/')
  } catch {
    // error handled by interceptor
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  registerLoading.value = true
  try {
    await registerApi(registerForm)
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = ''
  } catch {
    // error handled by interceptor
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.login-visual {
  flex: 1.2;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  overflow: hidden;

  .visual-bg {
    position: absolute;
    inset: 0;
  }

  .orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.5;
    animation: float 8s ease-in-out infinite;
  }

  .orb-1 {
    width: 400px;
    height: 400px;
    background: #a8edea;
    top: -100px;
    left: -100px;
  }

  .orb-2 {
    width: 300px;
    height: 300px;
    background: #fed6e3;
    bottom: -80px;
    right: -80px;
    animation-delay: 2s;
  }

  .orb-3 {
    width: 200px;
    height: 200px;
    background: #c2e9fb;
    top: 40%;
    right: 10%;
    animation-delay: 4s;
  }

  .visual-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: #fff;
    padding: 40px;
    max-width: 480px;
  }

  .brand-icon {
    font-size: 72px;
    margin-bottom: 24px;
    filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
  }

  .brand-title {
    font-size: 36px;
    font-weight: 800;
    line-height: 1.3;
    margin-bottom: 12px;
    letter-spacing: 1px;
  }

  .brand-subtitle {
    font-size: 16px;
    opacity: 0.85;
    margin-bottom: 48px;
    letter-spacing: 3px;
  }

  .feature-list {
    text-align: left;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .feature-item {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 15px;
    opacity: 0.9;
  }

  .feature-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #fff;
    box-shadow: 0 0 12px rgba(255, 255, 255, 0.8);
    flex-shrink: 0;
  }
}

.login-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;

  .form-wrapper {
    width: 100%;
    max-width: 380px;
    padding: 40px;
  }

  .form-header {
    margin-bottom: 28px;

    h2 {
      font-size: 26px;
      font-weight: 700;
      color: #1d2129;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: #86909c;
    }
  }

  .login-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 24px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      height: 1px;
    }
  }

  .submit-btn {
    width: 100%;
    margin-top: 8px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 4px;
    border-radius: 8px;
  }

  .form-footer {
    margin-top: 40px;
    text-align: center;
    font-size: 12px;
    color: #c9cdd4;
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(30px, -30px); }
  66% { transform: translate(-20px, 20px); }
}

@media (max-width: 900px) {
  .login-visual {
    display: none;
  }
}
</style>