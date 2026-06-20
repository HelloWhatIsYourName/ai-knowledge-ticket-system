import { http, unwrapData, type ApiResponse } from './http'

export interface LoginRequest {
  username: string
  password: string
}

export interface UserSummary {
  id: number
  username: string
  displayName: string
}

export interface MenuSummary {
  code: string
  name: string
  path: string
  icon?: string
}

export interface LoginResponse {
  tokenType: string
  accessToken: string
  expiresIn: number
  user: UserSummary
  roles: string[]
  permissions: string[]
  menus: MenuSummary[]
}

export interface CurrentUserResponse {
  user: UserSummary
  roles: string[]
  permissions: string[]
  menus: MenuSummary[]
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await http.post<ApiResponse<LoginResponse>>('/auth/login', request)

  return unwrapData(response.data)
}

export async function getCurrentUser(): Promise<CurrentUserResponse> {
  const response = await http.get<ApiResponse<CurrentUserResponse>>('/auth/me')

  return unwrapData(response.data)
}
