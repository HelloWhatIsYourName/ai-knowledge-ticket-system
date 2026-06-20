# Phase 28 Live Contract Verification Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix frontend/backend ticket contract drift discovered by a real local smoke run.

**Architecture:** Align frontend ticket enums and smoke request payloads with backend domain enums instead of test-only aliases. Keep backend behavior unchanged and make frontend tests use backend-real values for status, priority, and comment type.

**Tech Stack:** Vue 3, TypeScript, Vitest, Vite, Spring Boot smoke APIs, Bash.

---

### Task 1: Reproduce the Live Contract Failure

**Files:**
- Modify: `frontend/src/views/tickets/TicketListView.spec.ts`

- [x] ⭐ **Step 1: Run live backend smoke**

Run:

```bash
tools/smoke/phase7-backend-smoke.sh
```

Actual: FAIL at `createTicket`, status `400`, because the smoke payload sent `priority:"MEDIUM"` while backend accepts `LOW`, `NORMAL`, `HIGH`, `URGENT`.

- [x] ⭐ **Step 2: Add frontend regression expectations**

Update `TicketListView.spec.ts` to use backend-real values:

```ts
status: 'PENDING_ASSIGN'
priority: 'NORMAL'
```

Expected: the test fails until frontend label maps understand `PENDING_ASSIGN`, `PENDING_PROCESS`, and `NORMAL`.

### Task 2: Align Frontend Ticket Contract

**Files:**
- Modify: `frontend/src/api/tickets.ts`
- Modify: `frontend/src/views/ai/RagChatView.vue`
- Modify: `frontend/src/views/tickets/TicketListView.vue`
- Modify: `frontend/src/views/tickets/TicketDetailView.vue`
- Modify: `frontend/src/api/tickets.spec.ts`
- Modify: `frontend/src/views/ai/RagChatView.spec.ts`
- Modify: `frontend/src/views/tickets/TicketListView.spec.ts`
- Modify: `frontend/src/views/tickets/TicketDetailView.spec.ts`

- [x] ⭐ **Step 1: Replace stale priority/status values**

Use backend values:

```ts
export type TicketPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
export type TicketStatus = 'PENDING_ASSIGN' | 'PENDING_PROCESS' | 'PROCESSING' | 'RESOLVED' | 'CLOSED'
```

Update RAG transfer default priority from `MEDIUM` to `NORMAL`.

- [x] ⭐ **Step 2: Replace stale comment type values**

Use backend values:

```ts
export type TicketCommentType = 'USER_REPLY' | 'AGENT_REPLY' | 'INTERNAL_NOTE' | 'SYSTEM'
```

In ticket detail, derive available comment types from current permissions:

- `ticket:process` or `ticket:manage`: `AGENT_REPLY`, `INTERNAL_NOTE`
- otherwise: `USER_REPLY`

- [x] ⭐ **Step 3: Scan for stale frontend/smoke enum values**

Run:

```bash
rg -n "'REPLY'|\"REPLY\"|commentType: 'REPLY'|MEDIUM|status: 'PENDING'|TicketStatus = 'PENDING'|TicketCommentType = 'REPLY'" frontend/src tools/smoke docs/superpowers/plans/2026-06-20-phase-7-quality-and-thesis-materials-implementation-plan.md
```

Actual: no matches.

### Task 3: Align Smoke Script and Plan Evidence

**Files:**
- Modify: `tools/smoke/phase7-backend-smoke.sh`
- Modify: `docs/superpowers/plans/2026-06-20-phase-7-quality-and-thesis-materials-implementation-plan.md`

- [x] ⭐ **Step 1: Change smoke priority payload**

Change:

```json
"priority":"MEDIUM"
```

to:

```json
"priority":"NORMAL"
```

- [x] ⭐ **Step 2: Keep Phase 7 recorded command in sync**

Update the Phase 7 plan snippet so future smoke runs do not copy the stale enum.

### Task 4: Verify Real Usability Slice

**Files:**
- All files above.

- [x] ⭐ **Step 1: Run frontend tests**

Run:

```bash
cd frontend
npm run test
```

Actual: PASS, 19 files, 40 tests.

- [x] ⭐ **Step 2: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Actual: PASS, `vue-tsc --noEmit` and `vite build`.

- [x] ⭐ **Step 3: Restart local dev services**

Started:

- Backend: `http://127.0.0.1:8080`
- Frontend: `http://127.0.0.1:5174/`

- [x] ⭐ **Step 4: Rerun live backend smoke**

Run:

```bash
tools/smoke/phase7-backend-smoke.sh
```

Actual: PASS through admin/user/agent login, knowledge document creation, knowledge search, RAG ask, ticket creation, assignment, user/agent ticket lists, admin statistics, and 401/403 permission checks.

- [x] ⭐ **Step 5: Confirm frontend dev page responds**

Run:

```bash
curl -L http://127.0.0.1:5174/
```

Actual: PASS, Vite HTML returned.

### Task 5: Commit

**Files:**
- All files above.

- [x] ⭐ **Step 1: Commit contract fix**

Run:

```bash
git add frontend/src/api/tickets.ts frontend/src/api/tickets.spec.ts frontend/src/views/ai/RagChatView.vue frontend/src/views/ai/RagChatView.spec.ts frontend/src/views/tickets/TicketListView.vue frontend/src/views/tickets/TicketListView.spec.ts frontend/src/views/tickets/TicketDetailView.vue frontend/src/views/tickets/TicketDetailView.spec.ts tools/smoke/phase7-backend-smoke.sh docs/superpowers/plans/2026-06-20-phase-7-quality-and-thesis-materials-implementation-plan.md docs/superpowers/plans/2026-06-20-phase-28-live-contract-verification-fix-implementation-plan.md
git commit -m "fix: align frontend ticket contract with backend"
```
