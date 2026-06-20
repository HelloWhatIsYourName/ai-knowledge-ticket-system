import { http, unwrapData, type ApiResponse } from './http'

export interface SystemUser {
  id: number
  username: string
  displayName: string
  status: string
  roleIds: number[]
}

export interface SystemRole {
  id: number
  roleCode: string
  roleName: string
  dataScope?: string
  status: string
  sortOrder?: number
}

export interface SystemPermission {
  id: number
  permissionCode: string
  permissionName: string
  module: string
}

export async function listSystemUsers(limit = 100): Promise<SystemUser[]> {
  const response = await http.get<ApiResponse<SystemUser[]>>('/admin/users', {
    params: { limit }
  })

  return unwrapData(response.data)
}

export async function listSystemRoles(): Promise<SystemRole[]> {
  const response = await http.get<ApiResponse<SystemRole[]>>('/admin/roles')

  return unwrapData(response.data)
}

export async function listSystemPermissions(): Promise<SystemPermission[]> {
  const response = await http.get<ApiResponse<SystemPermission[]>>('/admin/permissions')

  return unwrapData(response.data)
}

export async function enableSystemUser(userId: number): Promise<void> {
  const response = await http.post<ApiResponse<void>>(`/admin/users/${userId}/enable`)

  unwrapData(response.data)
}

export async function disableSystemUser(userId: number): Promise<void> {
  const response = await http.post<ApiResponse<void>>(`/admin/users/${userId}/disable`)

  unwrapData(response.data)
}

export async function replaceUserRoles(userId: number, roleIds: number[]): Promise<void> {
  const response = await http.post<ApiResponse<void>>(`/admin/users/${userId}/roles`, { roleIds })

  unwrapData(response.data)
}
