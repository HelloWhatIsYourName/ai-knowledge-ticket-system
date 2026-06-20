# Phase 23 Demo Corpus RAG Evaluation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make demo corpus loading and live RAG evaluation repeatable, secret-safe, and suitable for thesis defense evidence.

**Architecture:** Add two parameterized smoke/evaluation scripts under `tools/smoke`: one loads `docs/demo/v1-demo-corpus.json` through the real knowledge API, and one runs `docs/evaluation/rag-evaluation-set.json` through the real RAG chat API and writes sanitized metrics. Extend the documentation coverage test so scripts, report text, and evaluation dataset cannot drift.

**Tech Stack:** Bash, curl, Node.js JSON helpers, Spring Boot API, Oracle Vector-backed knowledge ingestion, live SiliconFlow + DeepSeek provider path.

---

## File Structure

- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`
  - Adds a Phase 23 contract test for script presence, endpoint coverage, metric names, secret redaction, and report/dataset alignment.
- Create: `tools/smoke/phase23-load-demo-corpus.sh`
  - Logs in as admin and loads each demo corpus entry through `POST /api/kb/documents/text`.
- Create: `tools/smoke/phase23-run-rag-evaluation.sh`
  - Logs in as user, runs each evaluation case through `POST /api/ai/chat/ask`, and writes sanitized JSON results with aggregate metrics.
- Modify: `docs/evaluation/rag-live-evaluation-report.md`
  - Aligns all 20 questions with `rag-evaluation-set.json` and documents Phase 23 command/output usage.
- Modify: `docs/superpowers/plans/2026-06-20-phase-23-demo-corpus-rag-evaluation-implementation-plan.md`
  - Tracks Phase 23 execution with checked boxes and `⭐` markers after completion.

---

### Task 1: Lock Phase 23 Coverage With a Failing Test

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`
- Create: `docs/superpowers/plans/2026-06-20-phase-23-demo-corpus-rag-evaluation-implementation-plan.md`

- [x] ⭐ **Step 1: Write the failing test**

Add `phase23DemoCorpusAndRagEvaluationScriptsAreRepeatableAndSecretSafe` to assert:
- `tools/smoke/phase23-load-demo-corpus.sh` exists and covers `/api/auth/login`, `/api/kb/documents/text`, `docs/demo/v1-demo-corpus.json`, `categoryId`, and `token:redacted`.
- `tools/smoke/phase23-run-rag-evaluation.sh` exists and covers `/api/auth/login`, `/api/ai/chat/ask`, `docs/evaluation/rag-evaluation-set.json`, `RESULTS_PATH`, and metric keys `retrievalHitRate`, `answerUsefulRate`, `wrongTransferRate`, `missedTransferRate`.
- Neither script echoes bearer tokens or provider API keys.
- `docs/evaluation/rag-live-evaluation-report.md` contains both Phase 23 command names and every case id/question from `docs/evaluation/rag-evaluation-set.json`.

- [x] ⭐ **Step 2: Run test to verify it fails**

Run:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dtest=DocumentationCoverageTest test
```

Expected: FAIL because Phase 23 scripts do not exist yet.

---

### Task 2: Implement Repeatable Demo Corpus Loading

**Files:**
- Create: `tools/smoke/phase23-load-demo-corpus.sh`

- [x] ⭐ **Step 1: Create the loader script**

Implement a Bash script that:
- Uses `BASE_URL`, `ADMIN_USERNAME`, `PASSWORD`, and `CORPUS_PATH` environment overrides.
- Logs in with `/api/auth/login`.
- Reads `docs/demo/v1-demo-corpus.json`.
- Posts each entry to `/api/kb/documents/text` with `title`, `categoryId`, and `content`.
- Prints only case ids, titles, HTTP statuses, document ids, parse status, and `token:redacted`.
- Never prints bearer tokens or provider API keys.

- [x] ⭐ **Step 2: Verify shell syntax**

Run:

```bash
bash -n tools/smoke/phase23-load-demo-corpus.sh
```

Expected: PASS.

---

### Task 3: Implement Repeatable RAG Evaluation Runner

**Files:**
- Create: `tools/smoke/phase23-run-rag-evaluation.sh`

- [x] ⭐ **Step 1: Create the evaluation script**

Implement a Bash script that:
- Uses `BASE_URL`, `USER_USERNAME`, `PASSWORD`, `EVALUATION_SET_PATH`, `RESULTS_PATH`, `TOP_K`, and `MIN_SIMILARITY` environment overrides.
- Logs in with `/api/auth/login`.
- Runs every case in `docs/evaluation/rag-evaluation-set.json` through `/api/ai/chat/ask`.
- Writes sanitized JSON to `RESULTS_PATH`.
- Computes `retrievalHitRate`, `answerUsefulRate`, `wrongTransferRate`, and `missedTransferRate`.
- Uses keyword/source-hint heuristics as a repeatable first-pass score, leaving final human scoring in the report.
- Prints only ids, statuses, metric summary, output path, and `token:redacted`.

- [x] ⭐ **Step 2: Verify shell syntax**

Run:

```bash
bash -n tools/smoke/phase23-run-rag-evaluation.sh
```

Expected: PASS.

---

### Task 4: Align Live Evaluation Report With Dataset and Scripts

**Files:**
- Modify: `docs/evaluation/rag-live-evaluation-report.md`

- [x] ⭐ **Step 1: Update report instructions**

Add a command section:

```bash
tools/smoke/phase23-load-demo-corpus.sh
tools/smoke/phase23-run-rag-evaluation.sh
```

Document that `RESULTS_PATH` defaults to `/tmp/phase23-rag-evaluation-results.json` and can be overridden.

- [x] ⭐ **Step 2: Update all case rows**

Replace stale RAG-016 through RAG-020 questions with the exact current questions from `docs/evaluation/rag-evaluation-set.json`.

---

### Task 5: Verify and Commit Phase 23

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`
- Create: `tools/smoke/phase23-load-demo-corpus.sh`
- Create: `tools/smoke/phase23-run-rag-evaluation.sh`
- Modify: `docs/evaluation/rag-live-evaluation-report.md`
- Modify: `docs/superpowers/plans/2026-06-20-phase-23-demo-corpus-rag-evaluation-implementation-plan.md`

- [x] ⭐ **Step 1: Run focused verification**

Run:

```bash
bash -n tools/smoke/phase23-load-demo-corpus.sh
bash -n tools/smoke/phase23-run-rag-evaluation.sh
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dtest=DocumentationCoverageTest,RagEvaluationSetTest,DemoCorpusTest test
```

Expected: PASS.

- [x] ⭐ **Step 2: Run full backend tests**

Run:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn test
```

Expected: PASS.

- [x] ⭐ **Step 3: Mark this plan complete**

Change each completed checkbox to `- [x] ⭐`.

- [x] ⭐ **Step 4: Commit**

Run:

```bash
git add backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java tools/smoke/phase23-load-demo-corpus.sh tools/smoke/phase23-run-rag-evaluation.sh docs/evaluation/rag-live-evaluation-report.md docs/superpowers/plans/2026-06-20-phase-23-demo-corpus-rag-evaluation-implementation-plan.md
git commit -m "docs: add repeatable rag evaluation scripts"
```
