# Phase 10 Ticket Detail Comments Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Complete the first ticket handling loop in the frontend by adding ticket detail, reply/internal-note comments, and status transition actions.

**Architecture:** Extend the existing `tickets.ts` API boundary with explicit workflow action helpers, then add `/app/tickets/:ticketId` as the detail workspace. The list page links into detail, and the detail page loads ticket metadata, flow logs, comments, and exposes small action forms that call the backend workflow endpoints without introducing new state libraries.

**Tech Stack:** Vue 3, TypeScript, Vite, Vitest, Vue Test Utils, Vue Router, Axios.

---

## File Structure

```text
frontend/src/api/tickets.ts
frontend/src/api/tickets.spec.ts
frontend/src/views/tickets/TicketDetailView.vue
frontend/src/views/tickets/TicketDetailView.spec.ts
frontend/src/views/tickets/TicketListView.vue
frontend/src/views/tickets/TicketListView.spec.ts
frontend/src/router/index.ts
frontend/src/styles/main.css
docs/superpowers/plans/2026-06-20-phase-10-ticket-detail-comments-implementation-plan.md
```

## Task 1: Ticket Workflow API Helpers ⭐

**Files:**
- Modify: `frontend/src/api/tickets.ts`
- Modify: `frontend/src/api/tickets.spec.ts`

- [x] **Step 1: Write failing API tests**

Extend `frontend/src/api/tickets.spec.ts` to assert:

```ts
await getTicket(8)
expect(getMock).toHaveBeenCalledWith('/tickets/8')

await listTicketComments(8)
expect(getMock).toHaveBeenCalledWith('/tickets/8/comments')

await createTicketComment(8, { commentType: 'REPLY', content: '已收到' })
expect(postMock).toHaveBeenCalledWith('/tickets/8/comments', { commentType: 'REPLY', content: '已收到' })

await startTicket(8, '开始处理')
expect(postMock).toHaveBeenCalledWith('/tickets/8/start', { comment: '开始处理' })

await resolveTicket(8, '已解决')
expect(postMock).toHaveBeenCalledWith('/tickets/8/resolve', { comment: '已解决' })
```

- [x] **Step 2: Run API tests to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/api/tickets.spec.ts
```

Expected: fail because workflow helper exports do not exist.

- [x] **Step 3: Implement minimal workflow helpers**

Add `TicketActionRequest`, `startTicket`, `resolveTicket`, `reopenTicket`, `confirmCloseTicket`, `closeTicket`, and `assignTicket` to `tickets.ts`. Each function should post the backend DTO shape exactly.

- [x] **Step 4: Run API tests to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/api/tickets.spec.ts
```

Expected: pass.

## Task 2: Ticket Detail Workspace ⭐

**Files:**
- Create: `frontend/src/views/tickets/TicketDetailView.vue`
- Create: `frontend/src/views/tickets/TicketDetailView.spec.ts`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/styles/main.css`

- [x] **Step 1: Write failing detail page test**

Create `frontend/src/views/tickets/TicketDetailView.spec.ts`. Mock `vue-router` route params and the tickets API. Assert the page renders ticket number, title, description, AI transfer reason, flow log remark, reply content, internal note content, and buttons `开始处理` / `标记解决`.

- [x] **Step 2: Run detail test to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/views/tickets/TicketDetailView.spec.ts
```

Expected: fail because `TicketDetailView.vue` does not exist.

- [x] **Step 3: Implement detail workspace**

Create a page with:

- Header: ticket number, status, priority, source, created time.
- Main panel: title, description, AI summary/suggestion/transfer reason.
- Timeline panel: flow logs from `ticket.flowLogs`.
- Comments panel: comments from `listTicketComments(ticketId)`, with `REPLY` and `INTERNAL_NOTE` labels.
- Comment form: type select (`REPLY`, `INTERNAL_NOTE`) and textarea; submit calls `createTicketComment`.
- Actions: `startTicket`, `resolveTicket`, `reopenTicket`, `confirmCloseTicket`, and `closeTicket` buttons with a shared comment input.

- [x] **Step 4: Add detail route**

Add `/app/tickets/:ticketId` to `frontend/src/router/index.ts` before `/app/tickets/my` if needed so the static route remains unambiguous.

- [x] **Step 5: Run detail test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/views/tickets/TicketDetailView.spec.ts
```

Expected: pass.

## Task 3: List-to-Detail Entry ⭐

**Files:**
- Modify: `frontend/src/views/tickets/TicketListView.vue`
- Modify: `frontend/src/views/tickets/TicketListView.spec.ts`

- [x] **Step 1: Write failing list link assertion**

Extend `TicketListView.spec.ts` with a `RouterLink` stub and assert the ticket row contains `/app/tickets/1`.

- [x] **Step 2: Run list test to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/views/tickets/TicketListView.spec.ts
```

Expected: fail because the list page does not link to detail.

- [x] **Step 3: Add detail link**

Wrap each ticket title or ticket number in `RouterLink` to `/app/tickets/${ticket.id}` while keeping the dense table layout.

- [x] **Step 4: Run list test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/views/tickets/TicketListView.spec.ts
```

Expected: pass.

## Task 4: Verification and Plan Marking ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-10-ticket-detail-comments-implementation-plan.md`

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

- [x] **Step 4: Commit Phase 10 slice**

Run:

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-10-ticket-detail-comments-implementation-plan.md
git commit -m "feat: add ticket detail workflow"
```
