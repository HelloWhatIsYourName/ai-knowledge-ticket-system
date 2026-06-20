<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true

  try {
    await auth.login({ username: username.value, password: password.value })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    await router.push(redirect.startsWith('/app') ? redirect : auth.firstMenuPath)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-view">
    <form class="login-panel" @submit.prevent="submit">
      <RouterLink class="login-back" to="/">返回首页</RouterLink>
      <h2>登录系统</h2>
      <p>进入知识库问答与工单处理工作台。</p>

      <label>
        <span>用户名</span>
        <input v-model="username" name="username" autocomplete="username" required />
      </label>

      <label>
        <span>密码</span>
        <input v-model="password" name="password" type="password" autocomplete="current-password" required />
      </label>

      <p v-if="error" class="login-error" role="alert">{{ error }}</p>

      <button type="submit" :disabled="loading">
        {{ loading ? '登录中' : '进入系统' }}
      </button>
    </form>
  </main>
</template>
