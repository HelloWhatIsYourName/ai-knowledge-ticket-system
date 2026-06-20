# Phase 11 Knowledge Management Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Add a frontend knowledge-base workspace so demo users can create text documents, inspect parse status, and test retrieval results that feed RAG answers.

**Architecture:** Add a typed `knowledge.ts` API client for `/kb/documents`, `/kb/documents/text`, `/kb/search`, and document chunks. Add `/app/knowledge` as a quiet app-shell workspace with three panels: document list, text ingestion form, and retrieval test results. This keeps knowledge management separate from chat while using the same backend DTO boundary.

**Tech Stack:** Vue 3, TypeScript, Vite, Vitest, Vue Test Utils, Vue Router, Axios.

---

## File Structure

```text
frontend/src/api/knowledge.ts
frontend/src/api/knowledge.spec.ts
frontend/src/views/knowledge/KnowledgeBaseView.vue
frontend/src/views/knowledge/KnowledgeBaseView.spec.ts
frontend/src/router/index.ts
frontend/src/layouts/AppShell.vue
frontend/src/styles/main.css
docs/superpowers/plans/2026-06-20-phase-11-knowledge-management-frontend-implementation-plan.md
```

## Task 1: Knowledge API Client ⭐

**Files:**
- Create: `frontend/src/api/knowledge.ts`
- Create: `frontend/src/api/knowledge.spec.ts`

- [x] **Step 1: Write failing API tests**

Create `frontend/src/api/knowledge.spec.ts` to assert:

```ts
await listDocuments()
expect(getMock).toHaveBeenCalledWith('/kb/documents')

await createTextDocument({ title: '账号手册', content: '重置密码步骤', categoryId: 1 })
expect(postMock).toHaveBeenCalledWith('/kb/documents/text', { title: '账号手册', content: '重置密码步骤', categoryId: 1 })

await searchKnowledge({ query: '重置密码', topK: 4 })
expect(postMock).toHaveBeenCalledWith('/kb/search', { query: '重置密码', topK: 4 })

await listDocumentChunks(2)
expect(getMock).toHaveBeenCalledWith('/kb/documents/2/chunks')
```

- [x] **Step 2: Run API tests to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/api/knowledge.spec.ts
```

Expected: fail because `knowledge.ts` does not exist.

- [x] **Step 3: Implement minimal typed API client**

Create `knowledge.ts` exporting `KnowledgeDocument`, `KnowledgeChunk`, `KnowledgeSearchResult`, `CreateTextDocumentRequest`, `SearchKnowledgeRequest`, `listDocuments`, `createTextDocument`, `searchKnowledge`, `listDocumentChunks`, `enableDocument`, `disableDocument`, and `retryParseDocument`.

- [x] **Step 4: Run API tests to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/api/knowledge.spec.ts
```

Expected: pass.

## Task 2: Knowledge Workspace Page ⭐

**Files:**
- Create: `frontend/src/views/knowledge/KnowledgeBaseView.vue`
- Create: `frontend/src/views/knowledge/KnowledgeBaseView.spec.ts`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/layouts/AppShell.vue`
- Modify: `frontend/src/styles/main.css`

- [x] **Step 1: Write failing page test**

Create `frontend/src/views/knowledge/KnowledgeBaseView.spec.ts`. Mock `listDocuments`, `createTextDocument`, and `searchKnowledge`. Assert the page renders `知识库管理`, `文本录入`, `检索测试`, document title/status, and search result content. Assert submitting a text document calls `createTextDocument`.

- [x] **Step 2: Run page test to verify RED**

Run:

```bash
cd frontend
npm run test -- --run src/views/knowledge/KnowledgeBaseView.spec.ts
```

Expected: fail because `KnowledgeBaseView.vue` does not exist.

- [x] **Step 3: Implement knowledge workspace**

Create a three-panel workspace:

- Document list: title, category id, enabled state, parse status, retry count.
- Text ingestion form: title, category id, content; submit calls `createTextDocument` and refreshes `listDocuments`.
- Retrieval test: query, topK, minSimilarity; submit calls `searchKnowledge` and renders source title, similarity, and content snippet.
- Empty/error/loading states use existing common components.

- [x] **Step 4: Add route and fallback menu**

Add `/app/knowledge` in `router/index.ts`. Add `知识库` to `AppShell.vue` fallback menu between `AI 问答` and `我的工单`.

- [x] **Step 5: Run page test to verify GREEN**

Run:

```bash
cd frontend
npm run test -- --run src/views/knowledge/KnowledgeBaseView.spec.ts
```

Expected: pass.

## Task 3: Verification and Plan Marking ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-11-knowledge-management-frontend-implementation-plan.md`

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

- [x] **Step 4: Commit Phase 11 slice**

Run:

```bash
git add frontend docs/superpowers/plans/2026-06-20-phase-11-knowledge-management-frontend-implementation-plan.md
git commit -m "feat: add knowledge management frontend"
```
