# Phase 29 Vite API Proxy Login Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix browser login failures on the local frontend dev server by proxying `/api` requests to the Spring Boot backend.

**Architecture:** Keep frontend API calls relative to `/api` so production hosting remains portable, and add a Vite dev-server proxy for local development. Add a small regression test that guards the proxy setting because this failure only appears when using the browser against the dev server.

**Tech Stack:** Vite, Vue 3, Vitest, Axios, Spring Boot.

---

### Task 1: Reproduce Login 404

**Files:**
- No code changes.

- [x] ⭐ **Step 1: Verify backend login works directly**

Run:

```bash
curl -i -X POST http://127.0.0.1:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin_123456"}'
```

Actual: HTTP 200 from backend.

- [x] ⭐ **Step 2: Verify frontend dev server login path fails without proxy**

Run:

```bash
curl -i -X POST http://127.0.0.1:5174/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin_123456"}'
```

Actual: HTTP 404 from Vite dev server before the fix.

### Task 2: Add Regression Test

**Files:**
- Create: `frontend/src/config/viteConfig.spec.ts`

- [x] ⭐ **Step 1: Add failing proxy config test**

Test requires `vite.config.ts` to contain:

```ts
'/api'
target: 'http://127.0.0.1:8080'
changeOrigin: true
```

- [x] ⭐ **Step 2: Verify RED**

Run:

```bash
cd frontend
npm run test -- src/config/viteConfig.spec.ts
```

Actual: FAIL because `vite.config.ts` did not contain `/api`.

### Task 3: Add Vite Dev Proxy

**Files:**
- Modify: `frontend/vite.config.ts`

- [x] ⭐ **Step 1: Add `/api` proxy**

Add:

```ts
proxy: {
  '/api': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true
  }
}
```

- [x] ⭐ **Step 2: Verify focused test GREEN**

Run:

```bash
cd frontend
npm run test -- src/config/viteConfig.spec.ts
```

Actual: PASS, 1 test.

### Task 4: Verify Browser Login Path

**Files:**
- No code changes.

- [x] ⭐ **Step 1: Restart local dev services**

Started:

- Backend: `http://127.0.0.1:8080`
- Frontend: `http://127.0.0.1:5174/`

- [x] ⭐ **Step 2: Verify proxied login without printing token**

Run:

```bash
curl -sS -o /tmp/akt-proxy-login.json -w '%{http_code}' \
  -X POST http://127.0.0.1:5174/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin_123456"}'
```

Actual: HTTP 200. Parsed response: `success=true`, `user=admin`, `roles=["ADMIN"]`.

### Task 5: Verification and Commit

**Files:**
- Modify: `frontend/vite.config.ts`
- Create: `frontend/src/config/viteConfig.spec.ts`
- Create: `docs/superpowers/plans/2026-06-20-phase-29-vite-api-proxy-login-fix-implementation-plan.md`

- [x] ⭐ **Step 1: Run frontend tests**

Run:

```bash
cd frontend
npm run test
```

Actual: PASS, 20 files, 41 tests.

- [x] ⭐ **Step 2: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Actual: PASS, `vue-tsc --noEmit` and `vite build`.

- [x] ⭐ **Step 3: Run backend smoke**

Run:

```bash
tools/smoke/phase7-backend-smoke.sh
```

Actual: PASS through admin/user/agent login, knowledge creation/search, RAG ask, ticket creation/assignment, ticket lists, admin statistics, and 401/403 permission checks.

- [x] ⭐ **Step 4: Commit**

Run:

```bash
git add frontend/vite.config.ts frontend/src/config/viteConfig.spec.ts docs/superpowers/plans/2026-06-20-phase-29-vite-api-proxy-login-fix-implementation-plan.md
git commit -m "fix: proxy frontend api requests in dev"
```
