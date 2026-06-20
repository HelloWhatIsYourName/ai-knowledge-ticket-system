import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { getCurrentUser, login } from './auth'

vi.mock('./http', async () => {
  const actual = await vi.importActual<typeof import('./http')>('./http')

  return {
    ...actual,
    http: {
      get: vi.fn(),
      post: vi.fn()
    }
  }
})

const getMock = vi.mocked(http.get)
const postMock = vi.mocked(http.post)

describe('auth api', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('logs in through the auth endpoint', async () => {
    postMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: {
          tokenType: 'Bearer',
          accessToken: 'token-123',
          expiresIn: 3600,
          user: { id: 7, username: 'admin', displayName: '管理员' },
          roles: ['ADMIN'],
          permissions: ['admin:statistics:read'],
          menus: []
        },
        message: 'ok'
      }
    })

    await expect(login({ username: 'admin', password: 'secret' })).resolves.toMatchObject({
      accessToken: 'token-123'
    })
    expect(postMock).toHaveBeenCalledWith('/auth/login', { username: 'admin', password: 'secret' })
  })

  it('loads the current authenticated user', async () => {
    getMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: {
          user: { id: 7, username: 'admin', displayName: '管理员' },
          roles: ['ADMIN'],
          permissions: ['ticket:manage'],
          menus: [{ code: 'ai-chat', name: 'AI 问答', path: '/app/ai/chat' }]
        },
        message: 'ok'
      }
    })

    await expect(getCurrentUser()).resolves.toMatchObject({
      user: { username: 'admin' },
      roles: ['ADMIN']
    })
    expect(getMock).toHaveBeenCalledWith('/auth/me')
  })
})
