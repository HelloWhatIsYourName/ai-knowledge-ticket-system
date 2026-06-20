import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppShell from '../layouts/AppShell.vue'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/app',
    name: 'app',
    component: AppShell,
    children: [
      {
        path: '',
        name: 'app-home',
        component: PlaceholderView
      },
      {
        path: 'admin/dashboard',
        name: 'admin-dashboard',
        component: PlaceholderView
      },
      {
        path: ':pathMatch(.*)*',
        name: 'app-placeholder',
        component: PlaceholderView
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
