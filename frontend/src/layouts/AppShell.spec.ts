import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import AppShell from './AppShell.vue'
import { useAuthStore } from '../stores/auth'

describe('AppShell', () => {
  it('renders navigation labels from auth menus', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/:pathMatch(.*)*', component: { template: '<section />' } }]
    })
    const auth = useAuthStore()
    auth.$patch({
      user: { id: 7, username: 'admin', displayName: '管理员' },
      menus: [
        { code: 'admin-dashboard', name: '管理统计', path: '/app/admin/dashboard', icon: 'dashboard' },
        { code: 'ticket-workspace', name: '工单工作台', path: '/app/tickets', icon: 'ticket' }
      ]
    })
    await router.push('/app')
    await router.isReady()

    const wrapper = mount(AppShell, {
      global: {
        plugins: [pinia, router],
        stubs: {
          RouterView: { template: '<section />' }
        }
      }
    })

    expect(wrapper.text()).toContain('管理统计')
    expect(wrapper.text()).toContain('工单工作台')
    expect(wrapper.text()).toContain('管理员')
  })
})
