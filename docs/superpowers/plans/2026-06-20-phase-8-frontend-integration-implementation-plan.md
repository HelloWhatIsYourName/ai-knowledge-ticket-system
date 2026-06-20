# Phase 8 Frontend Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Add a credible public homepage plus the first Vue3 product shell slice so the backend can be demonstrated through a polished end-to-end application.

**Architecture:** Create a dedicated `frontend/` Vite application using Vue3, TypeScript, Vue Router, Pinia, Element Plus, Axios, ECharts, and GSAP only for isolated homepage scroll storytelling. The public route `/` uses a white premium SaaS homepage inspired by the user's reference page, while `/app` uses a quiet Claude/Zendesk-style product shell driven by backend auth menus. Later slices can add RAG chat, ticket workflow, knowledge management, and system administration forms without changing the shell.

**Tech Stack:** Vue 3, TypeScript, Vite, Vitest, Vue Test Utils, Pinia, Vue Router, Element Plus, Axios, ECharts, GSAP ScrollTrigger.

---

## File Structure

```text
frontend/package.json
frontend/index.html
frontend/tsconfig.json
frontend/tsconfig.node.json
frontend/vite.config.ts
frontend/vitest.config.ts
frontend/src/main.ts
frontend/src/App.vue
frontend/src/router/index.ts
frontend/src/stores/auth.ts
frontend/src/api/http.ts
frontend/src/api/auth.ts
frontend/src/api/adminStatistics.ts
frontend/src/components/home/HomeHero.vue
frontend/src/components/home/HomeMetrics.vue
frontend/src/components/home/HomeNarrative.vue
frontend/src/components/home/HomeCapabilityTable.vue
frontend/src/components/home/HomeFooter.vue
frontend/src/components/common/LoadingState.vue
frontend/src/components/common/ErrorState.vue
frontend/src/components/common/EmptyState.vue
frontend/src/layouts/AppShell.vue
frontend/src/views/HomeView.vue
frontend/src/views/LoginView.vue
frontend/src/views/admin/AdminDashboardView.vue
frontend/src/views/PlaceholderView.vue
frontend/src/styles/main.css
frontend/src/test/setup.ts
frontend/src/**/*.spec.ts
docs/spikes/phase-8-frontend-integration.md
docs/superpowers/specs/2026-06-20-public-homepage-product-shell-design.md
docs/superpowers/plans/2026-06-20-phase-8-frontend-integration-implementation-plan.md
docs/superpowers/specs/2026-06-19-ai-knowledge-ticket-v1-project-plan.md
沟通材料/2026-06-19-ai-knowledge-ticket-v1-project-plan.md
```

## Task 1: Frontend Project Scaffold

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/index.html`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tsconfig.node.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/vitest.config.ts`
- Create: `frontend/src/test/setup.ts`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/styles/main.css`
- Test: `frontend/src/App.spec.ts`
- Test: `frontend/src/router/index.spec.ts`

- [ ] **Step 1: Create package and test config**

Create a Vite/Vitest Vue project configuration with scripts:

```json
{
  "scripts": {
    "dev": "vite --host 127.0.0.1",
    "build": "vue-tsc -b && vite build",
    "test": "vitest run",
    "test:watch": "vitest"
  }
}
```

- [ ] **Step 2: Write failing app and router tests**

Create `frontend/src/App.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import App from './App.vue'

describe('App', () => {
  it('renders the application product name', () => {
    const wrapper = mount(App)
    expect(wrapper.text()).toContain('AI 知识库问答与工单协同处理平台')
  })
})
```

Create `frontend/src/router/index.spec.ts`:

```ts
import { describe, expect, it } from 'vitest'
import { routes } from './index'

describe('routes', () => {
  it('exposes public, login, and app routes', () => {
    expect(routes.map((route) => route.path)).toEqual(expect.arrayContaining(['/', '/login', '/app']))
  })
})
```

- [ ] **Step 3: Run frontend tests to verify RED**

```bash
cd frontend
npm install
npm run test
```

Expected: fail until `App.vue` renders the Chinese product name and `router/index.ts` exports routes.

- [ ] **Step 4: Implement minimal app, router, and global style**

Create `App.vue` with the product name and `<router-view />`. Create `main.ts` that mounts Vue, router, Pinia, and Element Plus. Create `router/index.ts` with routes for `/`, `/login`, and `/app`.

- [ ] **Step 5: Run frontend tests and build**

```bash
cd frontend
npm run test
npm run build
```

- [ ] **Step 6: Commit Task 1**

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-8-frontend-integration-implementation-plan.md
git commit -m "feat: scaffold frontend application"
```

## Task 2: Public Homepage and Motion Baseline

**Files:**
- Create: `frontend/src/views/HomeView.vue`
- Create: `frontend/src/components/home/HomeHero.vue`
- Create: `frontend/src/components/home/HomeMetrics.vue`
- Create: `frontend/src/components/home/HomeNarrative.vue`
- Create: `frontend/src/components/home/HomeCapabilityTable.vue`
- Create: `frontend/src/components/home/HomeFooter.vue`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/styles/main.css`
- Test: `frontend/src/views/HomeView.spec.ts`
- Test: `frontend/src/components/home/HomeNarrative.spec.ts`

- [ ] **Step 1: Write homepage render test**

Create `frontend/src/views/HomeView.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import HomeView from './HomeView.vue'

describe('HomeView', () => {
  it('renders the public homepage content', () => {
    const wrapper = mount(HomeView, {
      global: {
        stubs: {
          RouterLink: { template: '<a><slot /></a>' }
        }
      }
    })

    expect(wrapper.text()).toContain('AI 知识库问答与工单协同处理平台')
    expect(wrapper.text()).toContain('先回答，再流转')
    expect(wrapper.text()).toContain('知识库可追溯')
    expect(wrapper.text()).toContain('工单闭环')
    expect(wrapper.text()).toContain('管理端看得见')
  })
})
```

- [ ] **Step 2: Write motion safety test**

Create `frontend/src/components/home/HomeNarrative.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import HomeNarrative from './HomeNarrative.vue'

describe('HomeNarrative', () => {
  it('keeps narrative panels available without animation', () => {
    const wrapper = mount(HomeNarrative)

    expect(wrapper.text()).toContain('用户问题')
    expect(wrapper.text()).toContain('知识检索')
    expect(wrapper.text()).toContain('AI 回答')
    expect(wrapper.text()).toContain('转入工单')
  })
})
```

- [ ] **Step 3: Run homepage tests to verify RED**

```bash
cd frontend
npm run test -- --run src/views/HomeView.spec.ts src/components/home/HomeNarrative.spec.ts
```

Expected: fail until homepage components exist.

- [ ] **Step 4: Implement homepage components**

Create a white premium homepage with:

- Header navigation: Product, Workflow, Trust, Login.
- Hero headline: `AI 知识库问答与工单协同处理平台`.
- Hero subcopy: `先用知识库回答问题，再把复杂问题流转成可追踪工单。`
- CTAs: `进入系统`, `查看流程`.
- Metrics: RAG 问答, 向量检索, 工单闭环, RBAC 权限, 管理统计.
- Narrative panels: 用户问题, 知识检索, AI 回答, 转入工单, 管理统计.
- Capability table from the design spec.

- [ ] **Step 5: Add restrained motion CSS and GSAP hook**

Use CSS classes for opacity and transform transitions. If GSAP is installed, isolate ScrollTrigger setup inside `HomeNarrative.vue` with `onMounted` and `onUnmounted`. The reduced-motion branch must leave all content visible.

- [ ] **Step 6: Run homepage tests and build**

```bash
cd frontend
npm run test
npm run build
```

- [ ] **Step 7: Commit Task 2**

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-8-frontend-integration-implementation-plan.md docs/superpowers/specs/2026-06-20-public-homepage-product-shell-design.md
git commit -m "feat: add public homepage"
```

## Task 3: Auth API, Store, and RBAC Navigation

**Files:**
- Create: `frontend/src/api/http.ts`
- Create: `frontend/src/api/auth.ts`
- Create: `frontend/src/stores/auth.ts`
- Modify: `frontend/src/router/index.ts`
- Create: `frontend/src/layouts/AppShell.vue`
- Create: `frontend/src/views/LoginView.vue`
- Create: `frontend/src/views/PlaceholderView.vue`
- Test: `frontend/src/stores/auth.spec.ts`
- Test: `frontend/src/layouts/AppShell.spec.ts`

- [ ] **Step 1: Write auth store tests**

Assert login stores token, user, roles, permissions, and menus from the backend `ApiResponse<LoginResponse>` shape. Assert logout clears state.

- [ ] **Step 2: Write navigation test**

Mount `AppShell` with fake menus and assert menu labels render from `auth.menus`, not hard-coded duplicates.

- [ ] **Step 3: Run tests to verify RED**

```bash
cd frontend
npm run test -- --run src/stores/auth.spec.ts src/layouts/AppShell.spec.ts
```

- [ ] **Step 4: Implement API client and auth store**

Use Axios with `VITE_API_BASE_URL` defaulting to `/api`. Add request interceptor that attaches `Authorization: Bearer <token>` when present.

- [ ] **Step 5: Implement login view and app shell**

Use Element Plus form components. Login view posts username/password, stores token, and routes to the first visible menu or admin dashboard. App shell renders a quiet operational layout with sidebar navigation, user summary, and router content.

- [ ] **Step 6: Run tests and build**

```bash
cd frontend
npm run test
npm run build
```

- [ ] **Step 7: Commit Task 3**

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-8-frontend-integration-implementation-plan.md
git commit -m "feat: add frontend auth shell"
```

## Task 4: Admin Statistics Dashboard

**Files:**
- Create: `frontend/src/api/adminStatistics.ts`
- Create: `frontend/src/views/admin/AdminDashboardView.vue`
- Create: `frontend/src/components/common/LoadingState.vue`
- Create: `frontend/src/components/common/ErrorState.vue`
- Create: `frontend/src/components/common/EmptyState.vue`
- Test: `frontend/src/api/adminStatistics.spec.ts`
- Test: `frontend/src/views/admin/AdminDashboardView.spec.ts`

- [ ] **Step 1: Write API mapping tests**

Assert the admin statistics client calls:

```text
GET /admin/statistics/overview
GET /admin/statistics/ticket-categories
GET /admin/statistics/hot-questions
```

and unwraps `ApiResponse.data`.

- [ ] **Step 2: Write dashboard render test**

Mount dashboard with mocked API responses and assert these labels render:

```text
工单总量
待处理
平均处理时长
知识库命中率
热门问题
分类分布
```

- [ ] **Step 3: Run tests to verify RED**

```bash
cd frontend
npm run test -- --run src/api/adminStatistics.spec.ts src/views/admin/AdminDashboardView.spec.ts
```

- [ ] **Step 4: Implement statistics client**

Create typed methods:

```ts
getOverview()
getTicketCategoryStats(limit?: number)
getHotQuestions(limit?: number)
```

- [ ] **Step 5: Implement dashboard view**

Use Element Plus statistic/cards and ECharts for category and hot question charts. Include loading, empty, and error states.

- [ ] **Step 6: Run tests and build**

```bash
cd frontend
npm run test
npm run build
```

- [ ] **Step 7: Commit Task 4**

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-8-frontend-integration-implementation-plan.md
git commit -m "feat: add admin dashboard frontend"
```

## Task 5: Phase 8 Verification and Progress Marking

**Files:**
- Create: `docs/spikes/phase-8-frontend-integration.md`
- Modify: `docs/superpowers/plans/2026-06-20-phase-8-frontend-integration-implementation-plan.md`
- Modify: `docs/superpowers/specs/2026-06-19-ai-knowledge-ticket-v1-project-plan.md`
- Modify: `沟通材料/2026-06-19-ai-knowledge-ticket-v1-project-plan.md`

- [ ] **Step 1: Run frontend tests and build**

```bash
cd frontend
npm run test
npm run build
```

- [ ] **Step 2: Run backend tests**

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo test
```

- [ ] **Step 3: Start frontend dev server for manual preview**

```bash
cd frontend
npm run dev -- --port 5173
```

Use browser verification or screenshots for the public homepage, login shell, and dashboard layout if the backend is available. Check desktop and mobile viewports. Confirm homepage animation still presents all content when reduced motion is enabled.

- [ ] **Step 4: Write Phase 8 spike report**

Document test/build results, frontend routes, homepage motion checks, backend API contracts consumed, and remaining pages.

- [ ] **Step 5: Update project progress**

Mark frontend integration as started with public homepage, login/RBAC shell, and admin dashboard slice completed; note RAG chat and ticket workflow pages as next frontend slices.

- [ ] **Step 6: Commit Task 5**

```bash
git add docs/spikes/phase-8-frontend-integration.md docs/superpowers/plans/2026-06-20-phase-8-frontend-integration-implementation-plan.md docs/superpowers/specs/2026-06-19-ai-knowledge-ticket-v1-project-plan.md 沟通材料/2026-06-19-ai-knowledge-ticket-v1-project-plan.md
git commit -m "docs: verify phase 8 frontend integration"
```

## Current Execution Note

Phase 8 starts after Phase 7 backend evidence and thesis materials are complete. This phase intentionally delivers a public homepage plus a narrow but real product slice first: login, permission-aware app shell, and admin dashboard. Keep the homepage visually premium but credible, and keep logged-in product pages quiet and operational.
