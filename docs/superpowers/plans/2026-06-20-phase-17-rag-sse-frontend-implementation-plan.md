# Phase 17 RAG SSE Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Make the frontend RAG question flow consume the existing backend SSE endpoint first, while retaining the normal HTTP ask endpoint as an automatic fallback.

**Architecture:** Add a typed SSE client in `frontend/src/api/ragChat.ts` that uses `fetch` so the existing bearer token can be sent in headers. The page calls the stream client first, renders token chunks as they arrive, then applies the final metadata response; if streaming fails, it falls back to `askQuestion()`. Update acceptance docs so the first-version checklist reflects the implemented stream plus fallback path.

**Tech Stack:** Vue 3, TypeScript, Vitest, fetch ReadableStream, Spring MVC SSE endpoint already present in backend.

---

## File Structure

```text
frontend/src/api/ragChat.ts
frontend/src/api/ragChat.spec.ts
frontend/src/views/ai/RagChatView.vue
frontend/src/views/ai/RagChatView.spec.ts
docs/acceptance/v1-acceptance-checklist.md
docs/demo/v1-demo-runbook.md
docs/superpowers/plans/2026-06-20-phase-17-rag-sse-frontend-implementation-plan.md
```

## Task 1: Typed RAG Stream API Client ⭐

**Files:**
- Modify: `frontend/src/api/ragChat.ts`
- Modify: `frontend/src/api/ragChat.spec.ts`

- [x] **Step 1: Write failing stream API tests**

Extend `ragChat.spec.ts` to mock `globalThis.fetch` and assert `askQuestionStream()`:

```ts
await askQuestionStream(
  { question: '如何重置密码？', topK: 4 },
  { onToken: tokenHandler }
)

expect(fetchMock).toHaveBeenCalledWith(
  '/api/ai/chat/stream?question=%E5%A6%82%E4%BD%95%E9%87%8D%E7%BD%AE%E5%AF%86%E7%A0%81%EF%BC%9F&topK=4',
  expect.objectContaining({ method: 'GET' })
)
expect(tokenHandler).toHaveBeenCalledWith('第一段')
expect(result.answer).toBe('第一段第二段')
```

Use a fake `ReadableStream` containing:

```text
event: token
data: 第一段

event: token
data: 第二段

event: metadata
data: {"sessionId":7,"userMessageId":11,"assistantMessageId":12,"answer":"第一段第二段","canAnswer":true,"confidence":0.86,"transferSuggested":false,"transferReason":null,"citations":[]}

```

- [x] **Step 2: Run focused API test to verify RED**

Run:

```bash
cd frontend
npm run test -- src/api/ragChat.spec.ts
```

Expected: fail because `askQuestionStream` does not exist.

- [x] **Step 3: Implement `askQuestionStream`**

Add:

```ts
export interface RagStreamHandlers {
  onToken?: (token: string) => void
}
```

Implement `askQuestionStream(request, handlers)` so it builds `/ai/chat/stream` query params, prefixes the configured API base URL, sends `Authorization: Bearer ${localStorage.getItem('akt_token')}` when present, parses `event:` / `data:` SSE blocks, calls `handlers.onToken()` for token events, and returns the JSON metadata as `RagAnswerResponse`.

- [x] **Step 4: Run focused API test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- src/api/ragChat.spec.ts
```

Expected: pass.

## Task 2: RAG Chat Page Streaming with HTTP Fallback ⭐

**Files:**
- Modify: `frontend/src/views/ai/RagChatView.vue`
- Modify: `frontend/src/views/ai/RagChatView.spec.ts`

- [x] **Step 1: Write failing page tests**

Update the existing RAG page test mock to include `askQuestionStream`. Assert submitting a question calls `askQuestionStream`, renders streamed token text before final metadata, and does not call `askQuestion` when streaming succeeds.

Add a second test where `askQuestionStream` rejects and `askQuestion` resolves. Assert the page renders the HTTP answer and calls both methods in order.

- [x] **Step 2: Run focused page test to verify RED**

Run:

```bash
cd frontend
npm run test -- src/views/ai/RagChatView.spec.ts
```

Expected: fail because the page still calls `askQuestion()` directly.

- [x] **Step 3: Implement streaming submit path**

In `submitQuestion()`, call `askQuestionStream()` first. During token events, keep a temporary answer object with the streamed answer text and empty citations so the UI updates while the stream is active. After metadata returns, replace `answer.value` with the metadata response and fill ticket transfer fields. If the stream throws, call the existing `askQuestion()` path and keep the current error behavior only if both paths fail.

- [x] **Step 4: Run focused page test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- src/views/ai/RagChatView.spec.ts
```

Expected: pass.

## Task 3: Acceptance Documentation Sync ⭐

**Files:**
- Modify: `docs/acceptance/v1-acceptance-checklist.md`
- Modify: `docs/demo/v1-demo-runbook.md`

- [x] **Step 1: Update acceptance row for SSE**

Change the row for SSE streaming from remaining gap to implemented evidence. Mention backend `/api/ai/chat/stream`, frontend `askQuestionStream()`, and HTTP fallback through `/api/ai/chat/ask`.

- [x] **Step 2: Update demo runbook**

In the AI question section, add a short note that the frontend uses SSE for progressive answer rendering and falls back to normal HTTP if streaming fails.

## Task 4: Full Verification and Plan Marking ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-17-rag-sse-frontend-implementation-plan.md`

- [x] **Step 1: Run focused frontend tests**

Run:

```bash
cd frontend
npm run test -- src/api/ragChat.spec.ts src/views/ai/RagChatView.spec.ts
```

Expected: pass.

- [x] **Step 2: Run frontend test suite**

Run:

```bash
cd frontend
npm run test
```

Expected: pass.

- [x] **Step 3: Run frontend production build**

Run:

```bash
cd frontend
npm run build
```

Expected: pass.

- [x] **Step 4: Run backend focused SSE controller test**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dtest=RagChatControllerTest test
```

Expected: pass.

- [x] **Step 5: Mark completed plan tasks**

Append `⭐` to each completed task heading and change completed steps to `- [x]`.

- [x] **Step 6: Commit Phase 17 slice**

Run:

```bash
git add frontend/src/api/ragChat.ts frontend/src/api/ragChat.spec.ts frontend/src/views/ai/RagChatView.vue frontend/src/views/ai/RagChatView.spec.ts docs/acceptance/v1-acceptance-checklist.md docs/demo/v1-demo-runbook.md docs/superpowers/plans/2026-06-20-phase-17-rag-sse-frontend-implementation-plan.md
git commit -m "feat: stream rag answers in frontend"
```
