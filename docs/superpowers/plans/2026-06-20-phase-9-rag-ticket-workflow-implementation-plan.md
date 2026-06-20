# Phase 9 RAG Ticket Workflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Add the first real frontend business workflow: ask the RAG assistant, inspect citations, and transfer unresolved AI sessions into trackable tickets.

**Architecture:** Extend the existing Vue3 product shell with typed API clients for RAG chat, ticket categories, and tickets. The first workflow page lives at `/app/ai/chat`, keeps the UI quiet and utilitarian inside the app shell, and uses the backend DTOs directly so later streaming, assignment, and detail pages can be added without changing the API boundary.

**Tech Stack:** Vue 3, TypeScript, Vite, Vitest, Vue Test Utils, Pinia, Vue Router, Axios.

---

## File Structure

```text
frontend/src/api/ragChat.ts
frontend/src/api/ragChat.spec.ts
frontend/src/api/tickets.ts
frontend/src/api/tickets.spec.ts
frontend/src/views/ai/RagChatView.vue
frontend/src/views/ai/RagChatView.spec.ts
frontend/src/views/tickets/TicketListView.vue
frontend/src/views/tickets/TicketListView.spec.ts
frontend/src/router/index.ts
frontend/src/layouts/AppShell.vue
frontend/src/styles/main.css
docs/superpowers/plans/2026-06-20-phase-9-rag-ticket-workflow-implementation-plan.md
```

## Task 1: Typed RAG and Ticket API Clients ⭐

**Files:**
- Create: `frontend/src/api/ragChat.ts`
- Create: `frontend/src/api/ragChat.spec.ts`
- Create: `frontend/src/api/tickets.ts`
- Create: `frontend/src/api/tickets.spec.ts`

- [x] **Step 1: Write failing RAG API tests**

Create `frontend/src/api/ragChat.spec.ts` with tests that mock `http.get` and `http.post`. The tests must assert:

```ts
await askQuestion({ question: '如何重置密码？', topK: 4 })
expect(postMock).toHaveBeenCalledWith('/ai/chat/ask', { question: '如何重置密码？', topK: 4 })

await listSessions()
expect(getMock).toHaveBeenCalledWith('/ai/chat/sessions')

await listSessionMessages(12)
expect(getMock).toHaveBeenCalledWith('/ai/chat/sessions/12/messages')
```

- [x] **Step 2: Write failing ticket API tests**

Create `frontend/src/api/tickets.spec.ts` with tests that mock `http.get` and `http.post`. The tests must assert:

```ts
await listTicketCategories()
expect(getMock).toHaveBeenCalledWith('/ticket-categories', { params: { includeDisabled: false } })

await createTicketFromAiSession({
  sessionId: 7,
  assistantMessageId: 9,
  title: '无法登录',
  description: 'AI 建议转人工',
  priority: 'HIGH',
  transferReason: 'AI 置信度低'
})
expect(postMock).toHaveBeenCalledWith('/tickets/from-ai-session', expect.objectContaining({ sessionId: 7 }))

await listMyTickets()
expect(getMock).toHaveBeenCalledWith('/tickets/my')
```

- [x] **Step 3: Run API tests to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/api/ragChat.spec.ts src/api/tickets.spec.ts
```

Expected: fail because `ragChat.ts` and `tickets.ts` do not exist.

- [x] **Step 4: Implement minimal typed clients**

Create `ragChat.ts` exporting `AskQuestionRequest`, `RagAnswerResponse`, `RagCitation`, `AiSession`, `AiMessage`, `askQuestion`, `listSessions`, and `listSessionMessages`.

Create `tickets.ts` exporting `TicketPriority`, `TicketStatus`, `TicketCategory`, `TicketSummary`, `TicketDetail`, `TicketComment`, `CreateTicketFromAiSessionRequest`, `CreateTicketCommentRequest`, `listTicketCategories`, `createTicketFromAiSession`, `listMyTickets`, `listAssignedTickets`, `getTicket`, `listTicketComments`, and `createTicketComment`.

- [x] **Step 5: Run API tests to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/api/ragChat.spec.ts src/api/tickets.spec.ts
```

Expected: pass.

## Task 2: RAG Chat Workspace ⭐

**Files:**
- Create: `frontend/src/views/ai/RagChatView.vue`
- Create: `frontend/src/views/ai/RagChatView.spec.ts`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/layouts/AppShell.vue`
- Modify: `frontend/src/styles/main.css`

- [x] **Step 1: Write failing page test**

Create `frontend/src/views/ai/RagChatView.spec.ts`. Mock `askQuestion`, `listSessions`, `listSessionMessages`, `listTicketCategories`, and `createTicketFromAiSession`. The test must assert the page renders:

```ts
expect(wrapper.text()).toContain('AI 问答工作台')
expect(wrapper.text()).toContain('输入用户问题')
expect(wrapper.text()).toContain('引用来源')
expect(wrapper.text()).toContain('转为工单')
```

After clicking submit with a question, the test must assert `askQuestion` is called and the answer/citation text appears.

- [x] **Step 2: Run page test to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/views/ai/RagChatView.spec.ts
```

Expected: fail because `RagChatView.vue` does not exist.

- [x] **Step 3: Implement RAG chat page**

Create a quiet app-workspace page with:

- Left rail: recent AI sessions loaded from `listSessions()`.
- Main panel: question textarea, `topK` select, ask button, current answer, confidence, transfer suggestion, and citation cards.
- Right panel: transfer-to-ticket form shown after an answer with a `sessionId`.
- Failure state: inline message `问答请求失败，请稍后重试。`.
- Empty state: `输入用户问题后，系统会检索知识库并返回带引用的回答。`.

- [x] **Step 4: Add routes and fallback navigation**

Add `/app/ai/chat` to `frontend/src/router/index.ts`. Update `AppShell.vue` so it renders backend menus when present, otherwise it renders fallback links for `AI 问答`, `我的工单`, and `管理概览`.

- [x] **Step 5: Run page test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/views/ai/RagChatView.spec.ts
```

Expected: pass.

## Task 3: Ticket List Skeleton ⭐

**Files:**
- Create: `frontend/src/views/tickets/TicketListView.vue`
- Create: `frontend/src/views/tickets/TicketListView.spec.ts`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/styles/main.css`

- [x] **Step 1: Write failing ticket list test**

Create `frontend/src/views/tickets/TicketListView.spec.ts`. Mock `listMyTickets` and assert the page renders `我的工单`, ticket number, title, status, priority, and transfer reason from mocked data.

- [x] **Step 2: Run ticket list test to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/views/tickets/TicketListView.spec.ts
```

Expected: fail because `TicketListView.vue` does not exist.

- [x] **Step 3: Implement ticket list page**

Create `/app/tickets/my` with a dense table-like list showing ticket number, title, status, priority, source, transfer reason, and created time. Use `listMyTickets()` on mount and show `暂无工单` when empty.

- [x] **Step 4: Run ticket list test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/views/tickets/TicketListView.spec.ts
```

Expected: pass.

## Task 4: Verification and Plan Marking ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-9-rag-ticket-workflow-implementation-plan.md`

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

- [x] **Step 4: Commit Phase 9 slice**

Run:

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-9-rag-ticket-workflow-implementation-plan.md
git commit -m "feat: add rag ticket frontend workflow"
```
