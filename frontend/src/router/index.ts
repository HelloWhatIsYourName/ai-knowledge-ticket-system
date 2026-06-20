import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const LoginPlaceholder = {
  template: '<section>Login</section>'
}

const AppPlaceholder = {
  template: '<section>Product workspace</section>'
}

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPlaceholder
  },
  {
    path: '/app',
    name: 'app',
    component: AppPlaceholder
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
