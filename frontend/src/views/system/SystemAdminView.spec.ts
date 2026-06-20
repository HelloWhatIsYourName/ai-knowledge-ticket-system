import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import SystemAdminView from './SystemAdminView.vue'
import {
  disableSystemUser,
  listSystemPermissions,
  listSystemRoles,
  listSystemUsers,
  replaceUserRoles
} from '../../api/systemAdmin'

vi.mock('../../api/systemAdmin', () => ({
  disableSystemUser: vi.fn(),
  enableSystemUser: vi.fn(),
  listSystemPermissions: vi.fn(),
  listSystemRoles: vi.fn(),
  listSystemUsers: vi.fn(),
  replaceUserRoles: vi.fn()
}))

const listSystemUsersMock = vi.mocked(listSystemUsers)
const listSystemRolesMock = vi.mocked(listSystemRoles)
const listSystemPermissionsMock = vi.mocked(listSystemPermissions)
const disableSystemUserMock = vi.mocked(disableSystemUser)
const replaceUserRolesMock = vi.mocked(replaceUserRoles)

describe('SystemAdminView', () => {
  it('renders users, roles, permissions, and updates user access', async () => {
    listSystemUsersMock
      .mockResolvedValueOnce([
        { id: 7, username: 'agent', displayName: '客服专员', status: 'ACTIVE', roleIds: [1] }
      ])
      .mockResolvedValueOnce([
        { id: 7, username: 'agent', displayName: '客服专员', status: 'DISABLED', roleIds: [1, 2] }
      ])
      .mockResolvedValueOnce([
        { id: 7, username: 'agent', displayName: '客服专员', status: 'DISABLED', roleIds: [1, 2] }
      ])
    listSystemRolesMock.mockResolvedValue([
      { id: 1, roleCode: 'support', roleName: '客服', dataScope: 'OWN', status: 'ACTIVE', sortOrder: 1 },
      { id: 2, roleCode: 'admin', roleName: '管理员', dataScope: 'ALL', status: 'ACTIVE', sortOrder: 2 }
    ])
    listSystemPermissionsMock.mockResolvedValue([
      { id: 9, permissionCode: 'ticket:process', permissionName: '处理工单', module: 'ticket' },
      { id: 10, permissionCode: 'system:user:manage', permissionName: '管理用户', module: 'system' }
    ])
    disableSystemUserMock.mockResolvedValue(undefined)
    replaceUserRolesMock.mockResolvedValue(undefined)

    const wrapper = mount(SystemAdminView)
    await flushPromises()

    expect(wrapper.text()).toContain('系统管理')
    expect(wrapper.text()).toContain('agent')
    expect(wrapper.text()).toContain('客服专员')
    expect(wrapper.text()).toContain('ACTIVE')
    expect(wrapper.text()).toContain('客服')
    expect(wrapper.text()).toContain('管理员')
    expect(wrapper.text()).toContain('ticket')
    expect(wrapper.text()).toContain('处理工单')

    await wrapper.find('[data-testid="disable-user-7"]').trigger('click')
    await flushPromises()

    expect(disableSystemUserMock).toHaveBeenCalledWith(7)

    const adminCheckbox = wrapper.find('[data-testid="role-checkbox-2"]')
    await adminCheckbox.setValue(true)
    await wrapper.find('[data-testid="save-roles"]').trigger('click')
    await flushPromises()

    expect(replaceUserRolesMock).toHaveBeenCalledWith(7, [1, 2])
  })
})
