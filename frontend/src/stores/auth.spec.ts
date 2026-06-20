import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useAuthStore } from './auth'
import { login } from '../api/auth'

vi.mock('../api/auth', () => ({
  login: vi.fn()
}))

const loginMock = vi.mocked(login)

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    loginMock.mockReset()
  })

  it('stores login response token, user, roles, permissions, and menus', async () => {
    loginMock.mockResolvedValue({
      tokenType: 'Bearer',
      accessToken: 'token-123',
      expiresIn: 3600,
      user: {
        id: 7,
        username: 'admin',
        displayName: '管理员'
      },
      roles: ['ADMIN'],
      permissions: ['admin:statistics:read'],
      menus: [
        {
          code: 'admin-dashboard',
          name: '管理统计',
          path: '/app/admin/dashboard',
          icon: 'dashboard'
        }
      ]
    })

    const store = useAuthStore()
    await store.login({ username: 'admin', password: 'secret' })

    expect(store.token).toBe('token-123')
    expect(store.user?.username).toBe('admin')
    expect(store.roles).toEqual(['ADMIN'])
    expect(store.permissions).toEqual(['admin:statistics:read'])
    expect(store.menus).toHaveLength(1)
    expect(localStorage.getItem('akt_token')).toBe('token-123')
  })

  it('clears auth state on logout', async () => {
    const store = useAuthStore()
    store.$patch({
      token: 'token-123',
      user: { id: 7, username: 'admin', displayName: '管理员' },
      roles: ['ADMIN'],
      permissions: ['admin:statistics:read'],
      menus: [{ code: 'admin-dashboard', name: '管理统计', path: '/app/admin/dashboard', icon: 'dashboard' }]
    })
    localStorage.setItem('akt_token', 'token-123')

    store.logout()

    expect(store.token).toBe('')
    expect(store.user).toBeNull()
    expect(store.roles).toEqual([])
    expect(store.permissions).toEqual([])
    expect(store.menus).toEqual([])
    expect(localStorage.getItem('akt_token')).toBeNull()
  })
})
