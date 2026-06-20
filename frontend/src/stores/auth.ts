import { defineStore } from 'pinia'
import { login as loginRequest, type LoginRequest, type MenuSummary, type UserSummary } from '../api/auth'

interface AuthState {
  token: string
  user: UserSummary | null
  roles: string[]
  permissions: string[]
  menus: MenuSummary[]
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('akt_token') ?? '',
    user: null,
    roles: [],
    permissions: [],
    menus: []
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    firstMenuPath: (state) => state.menus[0]?.path ?? '/app'
  },
  actions: {
    async login(request: LoginRequest) {
      const response = await loginRequest(request)

      this.token = response.accessToken
      this.user = response.user
      this.roles = response.roles
      this.permissions = response.permissions
      this.menus = response.menus
      localStorage.setItem('akt_token', response.accessToken)
    },
    logout() {
      this.token = ''
      this.user = null
      this.roles = []
      this.permissions = []
      this.menus = []
      localStorage.removeItem('akt_token')
    }
  }
})
