# Phase 21 Live Rehearsal Audit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Record the current live rehearsal readiness truthfully and add a repeatable audit script that distinguishes pass, fail, and blocked prerequisites without printing secrets.

**Architecture:** Keep this as an operational evidence slice. A Bash audit script checks Docker services, backend/frontend reachability, provider key presence, and whether the Phase 19 preflight can be attempted. A Markdown report records the observed result from the current machine and points to the exact next action for unblocking real live-provider rehearsal.

**Tech Stack:** Bash, Markdown, Java 21, JUnit 5, AssertJ.

---

## File Structure

```text
backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java
docs/demo/v1-live-rehearsal-audit.md
tools/smoke/phase21-rehearsal-audit.sh
docs/demo/v1-demo-runbook.md
docs/superpowers/plans/2026-06-20-phase-21-live-rehearsal-audit-implementation-plan.md
```

## Task 1: Audit Coverage Guard ⭐

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`

- [x] **Step 1: Write failing coverage test**

Add a test that asserts:

```text
docs/demo/v1-live-rehearsal-audit.md exists
tools/smoke/phase21-rehearsal-audit.sh exists
audit report contains Docker services, Oracle 23ai, Redis, Backend, Frontend, AI_EMBEDDING_API_KEY, AI_CHAT_API_KEY, BLOCKED, token:redacted
audit script contains docker compose ps, /api/auth/me, 127.0.0.1:5174, phase19-demo-preflight.sh, token:redacted
audit script does not echo AI_CHAT_API_KEY or AI_EMBEDDING_API_KEY values
```

- [x] **Step 2: Run focused docs test to verify RED**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dtest=DocumentationCoverageTest test
```

Expected: fail because the audit report and script do not exist yet.

## Task 2: Rehearsal Audit Script ⭐

**Files:**
- Create: `tools/smoke/phase21-rehearsal-audit.sh`

- [x] **Step 1: Create audit script**

Create a Bash script that:

```text
checks docker compose ps
checks backend URL http://127.0.0.1:8080/api/auth/me
checks frontend URL http://127.0.0.1:5174/
checks whether AI_EMBEDDING_API_KEY and AI_CHAT_API_KEY are set, printing only present/missing
prints token:redacted as a secret-handling sentinel
attempts tools/smoke/phase19-demo-preflight.sh only when backend is reachable and both provider keys are present
exits 2 when rehearsal is blocked by missing prerequisites
exits 1 on unexpected check failure
exits 0 when the Phase 19 preflight passes
```

- [x] **Step 2: Run shell syntax check**

Run:

```bash
bash -n tools/smoke/phase21-rehearsal-audit.sh
```

Expected: pass.

- [x] **Step 3: Run audit script on current machine**

Run:

```bash
tools/smoke/phase21-rehearsal-audit.sh
```

Expected for current machine: Oracle and Redis are healthy, but rehearsal may return blocked if backend/frontend or `AI_CHAT_API_KEY` is missing.

## Task 3: Audit Report and Runbook Link ⭐

**Files:**
- Create: `docs/demo/v1-live-rehearsal-audit.md`
- Modify: `docs/demo/v1-demo-runbook.md`

- [x] **Step 1: Write audit report**

Record current observed status:

```text
Docker services: Oracle 23ai and Redis healthy
Backend: current observed state
Frontend: current observed state
AI_EMBEDDING_API_KEY: present or missing without value
AI_CHAT_API_KEY: present or missing without value
Phase 19 preflight: PASS or BLOCKED
Next action: start backend/frontend and configure missing provider key(s)
Secret handling: token:redacted, no key values printed
```

- [x] **Step 2: Link audit script and report from runbook**

Add the Phase 21 audit script and report to the runbook environment startup section.

## Task 4: Verification and Commit ⭐

**Files:**
- Modify: `docs/superpowers/plans/2026-06-20-phase-21-live-rehearsal-audit-implementation-plan.md`

- [x] **Step 1: Run focused docs test to verify GREEN**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dtest=DocumentationCoverageTest test
```

Expected: pass.

- [x] **Step 2: Run full backend tests**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn test
```

Expected: pass.

- [x] **Step 3: Mark completed plan tasks**

Append `⭐` to each completed task heading and change completed steps to `- [x]`.

- [x] **Step 4: Commit Phase 21 slice**

Run:

```bash
git add backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java docs/demo/v1-live-rehearsal-audit.md docs/demo/v1-demo-runbook.md tools/smoke/phase21-rehearsal-audit.sh docs/superpowers/plans/2026-06-20-phase-21-live-rehearsal-audit-implementation-plan.md
git commit -m "docs: add live rehearsal audit"
```
