# Phase 12 System Admin Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Add a frontend RBAC administration workspace for managing user status, assigning roles, and inspecting roles/permissions.

**Architecture:** Add a typed `systemAdmin.ts` API client for existing `/admin/users`, `/admin/roles`, and `/admin/permissions` endpoints. Add `/app/system` as a quiet app-shell management page with users in the main table and roles/permissions in side panels. Keep writes limited to backend-supported operations: enable/disable user and replace user roles.

**Tech Stack:** Vue 3, TypeScript, Vite, Vitest, Vue Test Utils, Vue Router, Axios.

---

## File Structure

```text
frontend/src/api/systemAdmin.ts
frontend/src/api/systemAdmin.spec.ts
frontend/src/views/system/SystemAdminView.vue
frontend/src/views/system/SystemAdminView.spec.ts
frontend/src/router/index.ts
frontend/src/layouts/AppShell.vue
frontend/src/styles/main.css
docs/superpowers/plans/2026-06-20-phase-12-system-admin-frontend-implementation-plan.md
```

## Task 1: System Admin API Client ⭐

**Files:**
- Create: `frontend/src/api/systemAdmin.ts`
- Create: `frontend/src/api/systemAdmin.spec.ts`

- [x] **Step 1: Write failing API tests**

Create `frontend/src/api/systemAdmin.spec.ts` and assert:

```ts
await listSystemUsers(50)
expect(getMock).toHaveBeenCalledWith('/admin/users', { params: { limit: 50 } })

await listSystemRoles()
expect(getMock).toHaveBeenCalledWith('/admin/roles')

await listSystemPermissions()
expect(getMock).toHaveBeenCalledWith('/admin/permissions')

await disableSystemUser(7)
expect(postMock).toHaveBeenCalledWith('/admin/users/7/disable')

await replaceUserRoles(7, [1, 2])
expect(postMock).toHaveBeenCalledWith('/admin/users/7/roles', { roleIds: [1, 2] })
```

- [x] **Step 2: Run API tests to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/api/systemAdmin.spec.ts
```

Expected: fail because `systemAdmin.ts` does not exist.

- [x] **Step 3: Implement minimal typed API client**

Create `systemAdmin.ts` exporting `SystemUser`, `SystemRole`, `SystemPermission`, `listSystemUsers`, `listSystemRoles`, `listSystemPermissions`, `enableSystemUser`, `disableSystemUser`, and `replaceUserRoles`.

- [x] **Step 4: Run API tests to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/api/systemAdmin.spec.ts
```

Expected: pass.

## Task 2: System Admin Workspace ⭐

**Files:**
- Create: `frontend/src/views/system/SystemAdminView.vue`
- Create: `frontend/src/views/system/SystemAdminView.spec.ts`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/layouts/AppShell.vue`
- Modify: `frontend/src/styles/main.css`

- [x] **Step 1: Write failing page test**

Create `frontend/src/views/system/SystemAdminView.spec.ts`. Mock system admin API functions. Assert the page renders `系统管理`, user username/display name/status, role names, permission modules, and calls `disableSystemUser` plus `replaceUserRoles` when the corresponding buttons are clicked.

- [x] **Step 2: Run page test to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/views/system/SystemAdminView.spec.ts
```

Expected: fail because `SystemAdminView.vue` does not exist.

- [x] **Step 3: Implement system admin workspace**

Create a two-column workspace:

- Main users panel: username, display name, status, assigned role labels, enable/disable buttons.
- Role assignment panel: selected user, checkbox list of roles, save button calling `replaceUserRoles`.
- Side panels: role list and permission list grouped visually by module.
- Loading/error/empty states use existing common components.

- [x] **Step 4: Add route and fallback menu**

Add `/app/system` to `router/index.ts`. Add `系统管理` to `AppShell.vue` fallback menu after `管理概览`.

- [x] **Step 5: Run page test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/views/system/SystemAdminView.spec.ts
```

Expected: pass.

## Task 3: Verification and Plan Marking ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-12-system-admin-frontend-implementation-plan.md`

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

- [x] **Step 4: Commit Phase 12 slice**

Run:

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-12-system-admin-frontend-implementation-plan.md
git commit -m "feat: add system admin frontend"
```
