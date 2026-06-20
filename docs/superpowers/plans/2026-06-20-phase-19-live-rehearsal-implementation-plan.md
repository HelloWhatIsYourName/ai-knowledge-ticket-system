# Phase 19 Live Rehearsal Evidence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Add repeatable final live-provider rehearsal and defense evidence materials without expanding V1 product scope.

**Architecture:** Keep Phase 19 as documentation and operational verification only. A backend documentation coverage test guards the existence and content of the live rehearsal checklist, RAG live evaluation report, preflight smoke script, and runbook links. The preflight script performs lightweight route and readiness checks while avoiding secrets in output.

**Tech Stack:** Markdown, Bash, Java 21, JUnit 5, AssertJ.

---

## File Structure

```text
backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java
docs/demo/v1-live-rehearsal-checklist.md
docs/demo/v1-demo-runbook.md
docs/evaluation/rag-live-evaluation-report.md
docs/acceptance/v1-acceptance-checklist.md
tools/smoke/phase19-demo-preflight.sh
docs/superpowers/plans/2026-06-20-phase-19-live-rehearsal-implementation-plan.md
```

## Task 1: Documentation Coverage Guard ⭐

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`
- Create later in Task 2: `docs/demo/v1-live-rehearsal-checklist.md`
- Create later in Task 3: `docs/evaluation/rag-live-evaluation-report.md`
- Create later in Task 4: `tools/smoke/phase19-demo-preflight.sh`

- [x] **Step 1: Write the failing coverage test**

Add a test method that reads the rehearsal checklist, live report, and preflight script. Assert that:

```text
docs/demo/v1-live-rehearsal-checklist.md exists
docs/evaluation/rag-live-evaluation-report.md exists
tools/smoke/phase19-demo-preflight.sh exists
checklist contains /app/demo, /app/knowledge, /app/ai/chat, /app/tickets/my, /app/tickets/assigned, /app/admin/dashboard, /app/system
checklist contains Oracle 23ai, Redis, SiliconFlow, DeepSeek, token:redacted
report contains docs/evaluation/rag-evaluation-set.json
report contains RAG-001 and RAG-020
report contains 检索命中率, 回答有用率, 误转工单率, 应转未转率
script contains /api/auth/login, /api/auth/me, /api/kb/search, /api/ai/chat/ask, /api/ai/chat/stream, /api/admin/statistics/overview
script contains token:redacted
script does not contain echo "$ADMIN_TOKEN" or echo "$USER_TOKEN"
```

- [x] **Step 2: Run focused backend docs test to verify RED**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dtest=DocumentationCoverageTest test
```

Expected: fail because the new Phase 19 checklist, report, and script do not exist yet.

## Task 2: Live Rehearsal Checklist ⭐

**Files:**
- Create: `docs/demo/v1-live-rehearsal-checklist.md`
- Modify: `docs/demo/v1-demo-runbook.md`

- [x] **Step 1: Write the checklist document**

Create a rehearsal checklist with these sections:

```text
1. Preflight Environment
2. Provider Readiness
3. Demo Data Loaded
4. Frontend Route Walkthrough
5. RAG Evidence Capture
6. Ticket Workflow Evidence Capture
7. Admin Evidence Capture
8. Failure Fallbacks
9. Final Defense Evidence Pack
```

Include explicit checks for Oracle 23ai, Redis, backend, frontend, SiliconFlow embeddings, DeepSeek chat, `token:redacted`, and every V1 demo route.

- [x] **Step 2: Link the checklist from the runbook**

Add a short note near the top of `docs/demo/v1-demo-runbook.md` pointing to `docs/demo/v1-live-rehearsal-checklist.md` as the final rehearsal checklist.

## Task 3: RAG Live Evaluation Report ⭐

**Files:**
- Create: `docs/evaluation/rag-live-evaluation-report.md`
- Modify: `docs/demo/v1-demo-runbook.md`

- [x] **Step 1: Write the live report template**

Create a report that:

```text
references docs/evaluation/rag-evaluation-set.json
lists RAG-001 through RAG-020
defines columns for retrieval hit, useful answer, transfer suggested, transfer expected, notes, citation evidence
summarizes 检索命中率, 回答有用率, 误转工单率, 应转未转率
records provider mode, corpus version, backend commit, frontend commit, evaluation date
```

- [x] **Step 2: Link the report from the runbook**

In the RAG question section of `docs/demo/v1-demo-runbook.md`, link `docs/evaluation/rag-live-evaluation-report.md` as the place to record live-provider results.

## Task 4: Demo Preflight Script ⭐

**Files:**
- Create: `tools/smoke/phase19-demo-preflight.sh`
- Modify: `docs/demo/v1-demo-runbook.md`

- [x] **Step 1: Write the preflight script**

Create a Bash script that:

```text
uses BASE_URL default http://127.0.0.1:8080
uses ADMIN_USERNAME, USER_USERNAME, PASSWORD env vars
logs in through /api/auth/login
prints token:redacted instead of JWT values
checks /api/auth/me
checks /api/kb/search with a password reset query
checks /api/ai/chat/ask
checks /api/ai/chat/stream with curl --max-time
checks /api/admin/statistics/overview
prints concise status lines
exits non-zero on unexpected HTTP status
```

- [x] **Step 2: Link the script from the runbook**

Add `tools/smoke/phase19-demo-preflight.sh` to the environment startup section and explain that it is lighter than the Phase 7 full mutation smoke.

## Task 5: Acceptance Sync and Verification ⭐

**Files:**
- Modify: `docs/acceptance/v1-acceptance-checklist.md`
- Modify: `docs/superpowers/plans/2026-06-20-phase-19-live-rehearsal-implementation-plan.md`

- [x] **Step 1: Update acceptance summary**

Update the final summary so the remaining V1 preparation points to the new live-provider rehearsal checklist and live evaluation report rather than an undefined future step.

- [x] **Step 2: Run focused docs test to verify GREEN**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dtest=DocumentationCoverageTest test
```

Expected: pass.

- [x] **Step 3: Run full backend tests**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn test
```

Expected: pass.

- [x] **Step 4: Run frontend tests**

Run:

```bash
cd frontend
npm run test
```

Expected: pass.

- [x] **Step 5: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Expected: pass.

- [x] **Step 6: Mark completed plan tasks**

Append `⭐` to each completed task heading and change completed steps to `- [x]`.

- [x] **Step 7: Commit Phase 19 slice**

Run:

```bash
git add backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java docs/demo/v1-live-rehearsal-checklist.md docs/demo/v1-demo-runbook.md docs/evaluation/rag-live-evaluation-report.md docs/acceptance/v1-acceptance-checklist.md tools/smoke/phase19-demo-preflight.sh docs/superpowers/plans/2026-06-20-phase-19-live-rehearsal-implementation-plan.md
git commit -m "docs: add live rehearsal evidence"
```
