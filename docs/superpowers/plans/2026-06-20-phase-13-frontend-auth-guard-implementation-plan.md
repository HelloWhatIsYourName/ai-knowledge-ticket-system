# Phase 13 Frontend Auth Guard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Make the frontend demo robust by restoring authenticated user state from `/auth/me`, guarding `/app` routes, and returning users to the requested page after login.

**Architecture:** Extend the existing auth API/store rather than adding a new auth layer. Add a route guard helper in `router/index.ts` that checks `/app` route metadata, restores the current user when a token exists, and redirects anonymous users to `/login?redirect=...`. Update `LoginView` to honor the redirect query after successful login.

**Tech Stack:** Vue 3, TypeScript, Vite, Vitest, Vue Test Utils, Pinia, Vue Router, Axios.

---

## File Structure

```text
frontend/src/api/auth.ts
frontend/src/api/auth.spec.ts
frontend/src/stores/auth.ts
frontend/src/stores/auth.spec.ts
frontend/src/router/index.ts
frontend/src/router/index.spec.ts
frontend/src/views/LoginView.vue
docs/superpowers/plans/2026-06-20-phase-13-frontend-auth-guard-implementation-plan.md
```

## Task 1: Auth API and Store Session Restore ⭐

**Files:**
- Modify: `frontend/src/api/auth.ts`
- Create: `frontend/src/api/auth.spec.ts`
- Modify: `frontend/src/stores/auth.ts`
- Modify: `frontend/src/stores/auth.spec.ts`

- [x] **Step 1: Write failing auth API tests**

Create `frontend/src/api/auth.spec.ts` asserting `login()` posts `/auth/login` and `getCurrentUser()` gets `/auth/me`.

- [x] **Step 2: Write failing store restore test**

Extend `frontend/src/stores/auth.spec.ts` to mock `getCurrentUser()` and assert `loadCurrentUser()` fills user, roles, permissions, and menus while keeping the token from `localStorage`.

- [x] **Step 3: Run tests to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/api/auth.spec.ts src/stores/auth.spec.ts
```

Expected: fail because `getCurrentUser` and `loadCurrentUser` do not exist.

- [x] **Step 4: Implement auth API and store restore**

Add `CurrentUserResponse`, `getCurrentUser()`, and `loadCurrentUser()` to the existing auth modules. If `loadCurrentUser()` fails, it should call `logout()` and rethrow.

- [x] **Step 5: Run tests to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/api/auth.spec.ts src/stores/auth.spec.ts
```

Expected: pass.

## Task 2: Route Guard and Login Redirect ⭐

**Files:**
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/router/index.spec.ts`
- Modify: `frontend/src/views/LoginView.vue`

- [x] **Step 1: Write failing router guard tests**

Extend `frontend/src/router/index.spec.ts` to assert `/app` has `meta.requiresAuth === true`, anonymous navigation to `/app/ai/chat` resolves to `/login?redirect=/app/ai/chat`, and authenticated-but-unhydrated navigation calls `loadCurrentUser()`.

- [x] **Step 2: Run router test to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/router/index.spec.ts
```

Expected: fail because guard helper and route metadata do not exist.

- [x] **Step 3: Implement route guard helper**

Add `resolveAuthNavigation(to, auth = useAuthStore())` and install it via `router.beforeEach`. Mark the `/app` route with `meta: { requiresAuth: true }`.

- [x] **Step 4: Honor login redirect**

Update `LoginView.vue` so successful login pushes `route.query.redirect` when it is a string starting with `/app`; otherwise push `auth.firstMenuPath`.

- [x] **Step 5: Run router test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/router/index.spec.ts
```

Expected: pass.

## Task 3: Verification and Plan Marking ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-13-frontend-auth-guard-implementation-plan.md`

- [x] **Step 1: Run full frontend test suite**

Run:

```bash
cd frontend
npm run test
```

Expected: all frontend tests pass.

- [x] **Step 2: Run frontend production build**

Run:

```bash
cd frontend
npm run build
```

Expected: TypeScript and Vite build complete successfully.

- [x] **Step 3: Mark completed plan tasks**

For each implemented and verified task heading, append `⭐` and change its steps from `- [ ]` to `- [x]`.

- [x] **Step 4: Commit Phase 13 slice**

Run:

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-13-frontend-auth-guard-implementation-plan.md
git commit -m "feat: add frontend auth guard"
```
