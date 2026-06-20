import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const HomePlaceholder = {
  template: '<section>Public homepage</section>'
}

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
    component: HomePlaceholder
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
