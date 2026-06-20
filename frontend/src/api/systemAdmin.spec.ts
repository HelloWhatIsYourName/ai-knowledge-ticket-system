import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import {
  disableSystemUser,
  enableSystemUser,
  listSystemPermissions,
  listSystemRoles,
  listSystemUsers,
  replaceUserRoles
} from './systemAdmin'

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

describe('systemAdmin api', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('loads users, roles, and permissions', async () => {
    getMock
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ id: 7, username: 'agent', displayName: '客服', status: 'ACTIVE', roleIds: [1] }],
          message: 'ok'
        }
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ id: 1, roleCode: 'support', roleName: '客服', status: 'ACTIVE' }],
          message: 'ok'
        }
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ id: 9, permissionCode: 'ticket:process', permissionName: '处理工单', module: 'ticket' }],
          message: 'ok'
        }
      })

    await expect(listSystemUsers(50)).resolves.toHaveLength(1)
    await expect(listSystemRoles()).resolves.toHaveLength(1)
    await expect(listSystemPermissions()).resolves.toHaveLength(1)
    expect(getMock).toHaveBeenNthCalledWith(1, '/admin/users', { params: { limit: 50 } })
    expect(getMock).toHaveBeenNthCalledWith(2, '/admin/roles')
    expect(getMock).toHaveBeenNthCalledWith(3, '/admin/permissions')
  })

  it('updates user status and roles', async () => {
    postMock
      .mockResolvedValueOnce({ data: { success: true, data: null, message: 'ok' } })
      .mockResolvedValueOnce({ data: { success: true, data: null, message: 'ok' } })
      .mockResolvedValueOnce({ data: { success: true, data: null, message: 'ok' } })

    await expect(disableSystemUser(7)).resolves.toBeUndefined()
    await expect(enableSystemUser(7)).resolves.toBeUndefined()
    await expect(replaceUserRoles(7, [1, 2])).resolves.toBeUndefined()
    expect(postMock).toHaveBeenNthCalledWith(1, '/admin/users/7/disable')
    expect(postMock).toHaveBeenNthCalledWith(2, '/admin/users/7/enable')
    expect(postMock).toHaveBeenNthCalledWith(3, '/admin/users/7/roles', { roleIds: [1, 2] })
  })
})
