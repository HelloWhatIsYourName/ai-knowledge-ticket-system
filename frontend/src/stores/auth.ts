import { defineStore } from 'pinia'
import {
  getCurrentUser,
  login as loginRequest,
  type CurrentUserResponse,
  type LoginRequest,
  type MenuSummary,
  type UserSummary
} from '../api/auth'

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
    applyCurrentUser(response: CurrentUserResponse) {
      this.user = response.user
      this.roles = response.roles
      this.permissions = response.permissions
      this.menus = response.menus
    },
    async login(request: LoginRequest) {
      const response = await loginRequest(request)

      this.token = response.accessToken
      this.applyCurrentUser(response)
      localStorage.setItem('akt_token', response.accessToken)
    },
    async loadCurrentUser() {
      try {
        const response = await getCurrentUser()
        this.applyCurrentUser(response)
      } catch (err) {
        this.logout()
        throw err
      }
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
