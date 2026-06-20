# Phase 15 Demo Corpus Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Add a ready-to-load demo knowledge corpus that supports the RAG evaluation set and the defense demo path.

**Architecture:** Store the demo corpus under `docs/demo/` as both JSON and Markdown. The JSON uses the same fields accepted by the frontend text-ingestion form (`title`, `categoryId`, `content`) plus source hints for evaluation coverage. A backend documentation test validates structure and confirms the corpus covers all non-transfer evaluation cases by source hint.

**Tech Stack:** Java 21, JUnit 5, AssertJ, Jackson, Markdown/JSON documentation.

---

## File Structure

```text
docs/demo/v1-demo-corpus.json
docs/demo/v1-demo-corpus.md
docs/demo/v1-demo-runbook.md
backend/src/test/java/com/example/aiticket/docs/DemoCorpusTest.java
docs/superpowers/plans/2026-06-20-phase-15-demo-corpus-implementation-plan.md
```

## Task 1: Corpus Structure Test ⭐

**Files:**
- Create: `backend/src/test/java/com/example/aiticket/docs/DemoCorpusTest.java`

- [x] **Step 1: Write failing corpus test**

Create a test that reads `../docs/demo/v1-demo-corpus.json` and `../docs/evaluation/rag-evaluation-set.json`. Assert the corpus has at least 10 documents, every document has `id`, `title`, `categoryId`, `sourceHints`, and `content`, and every non-transfer evaluation case has its `expectedSourceHint` covered by at least one document's `sourceHints`.

- [x] **Step 2: Run test to verify RED**

Run:

```bash
cd backend
mvn -Dtest=DemoCorpusTest test
```

Expected: fail because `v1-demo-corpus.json` does not exist.

## Task 2: Demo Corpus Files ⭐

**Files:**
- Create: `docs/demo/v1-demo-corpus.json`
- Create: `docs/demo/v1-demo-corpus.md`
- Modify: `docs/demo/v1-demo-runbook.md`

- [x] **Step 1: Create JSON corpus**

Add at least 15 text documents covering account login, permissions, VPN/network, device support, reimbursement, office process, contract approval, emergency database incident, and sensitive information boundaries.

- [x] **Step 2: Create Markdown guide**

Document how to load the corpus through `/app/knowledge`, which questions it supports, and how to use it with `docs/evaluation/rag-evaluation-set.json`.

- [x] **Step 3: Link corpus from runbook**

Update the demo runbook knowledge-preparation section to point to `docs/demo/v1-demo-corpus.json`.

- [x] **Step 4: Run corpus test to verify GREEN**

Run:

```bash
cd backend
mvn -Dtest=DemoCorpusTest test
```

Expected: pass.

## Task 3: Verification and Plan Marking ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-15-demo-corpus-implementation-plan.md`

- [x] **Step 1: Run frontend test suite**

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

- [x] **Step 4: Commit Phase 15 slice**

Run:

```bash
git add docs/demo backend/src/test/java/com/example/aiticket/docs/DemoCorpusTest.java docs/superpowers/plans/2026-06-20-phase-15-demo-corpus-implementation-plan.md
git commit -m "docs: add demo knowledge corpus"
```
