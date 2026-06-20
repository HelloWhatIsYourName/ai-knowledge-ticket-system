# Phase 31 Acceptance Evidence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a repeatable acceptance evidence script that records backend smoke, frontend smoke, frontend tests/build, and documentation coverage in a sanitized Markdown report.

**Architecture:** Keep the evidence collector in `tools/smoke/` so it can be run from the repository root after backend and frontend services are started. The script delegates to existing smoke scripts, captures command logs, redacts secrets through a shared `redact_log` function, writes a Markdown report to `REPORT_PATH`, and exits nonzero if any check fails.

**Tech Stack:** Bash, curl-based smoke scripts, Maven/JUnit, Vitest, Vite build, Markdown report output.

---

### Task 1: Add Regression Coverage

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`

- [x] ⭐ **Step 1: Add a failing script coverage test**

Add a test requiring `tools/smoke/phase31-acceptance-evidence.sh` to include:

- `REPORT_PATH`
- `phase7-backend-smoke.sh`
- `phase30-frontend-dev-smoke.sh`
- `npm run test`
- `npm run build`
- `mvn -Dtest=DocumentationCoverageTest test`
- `token:redacted`
- `redact_log`

Also assert the script does not echo `ADMIN_TOKEN`, `USER_TOKEN`, `AI_CHAT_API_KEY`, or `AI_EMBEDDING_API_KEY`.

- [x] ⭐ **Step 2: Verify RED**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH \
mvn -Dtest=DocumentationCoverageTest test
```

Actual: FAIL because `../tools/smoke/phase31-acceptance-evidence.sh` did not exist.

### Task 2: Add Acceptance Evidence Script

**Files:**
- Create: `tools/smoke/phase31-acceptance-evidence.sh`

- [x] ⭐ **Step 1: Implement command runner**

Create `run_check()` that:

- runs a command from the repository root,
- captures raw stdout/stderr to a temporary log,
- sanitizes the log through `redact_log`,
- records PASS/FAIL in an in-memory Markdown table,
- continues after failures so the report still contains all attempted checks.

- [x] ⭐ **Step 2: Implement report writer**

Create `write_report()` that writes:

- generation time,
- current branch,
- current commit,
- secret sentinel `token:redacted`,
- PASS/FAIL check table,
- tail excerpts from sanitized logs.

- [x] ⭐ **Step 3: Wire required checks**

Run the following checks:

```bash
tools/smoke/phase7-backend-smoke.sh
tools/smoke/phase30-frontend-dev-smoke.sh
cd frontend && npm run test
cd frontend && npm run build
cd backend && mvn -Dtest=DocumentationCoverageTest test
```

For the Maven check, prefer local OpenJDK 21 when it is available.

- [x] ⭐ **Step 4: Verify script syntax and focused GREEN**

Run:

```bash
bash -n tools/smoke/phase31-acceptance-evidence.sh
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH \
mvn -Dtest=DocumentationCoverageTest test
```

Actual: PASS, `DocumentationCoverageTest` 8 tests.

### Task 3: Run Acceptance Evidence Collection

**Files:**
- No code changes.

- [x] ⭐ **Step 1: Run the evidence collector**

Run:

```bash
tools/smoke/phase31-acceptance-evidence.sh
```

Actual: PASS. Report written to:

```text
/var/folders/qm/7s5b8sr96_99wh2x_9g1mwbr0000gn/T/phase31-acceptance-evidence.md
```

- [x] ⭐ **Step 2: Inspect report summary**

Observed report checks:

- Backend smoke: PASS
- Frontend dev smoke: PASS
- Frontend tests: PASS
- Frontend build: PASS
- Backend documentation coverage: PASS

- [x] ⭐ **Step 3: Scan generated report and logs for obvious leaked secrets**

Run:

```bash
rg -- "sk-|accessToken|Bearer [A-Za-z0-9]|AI_CHAT_API_KEY=.*[^redacted]|AI_EMBEDDING_API_KEY=.*[^redacted]" \
  /var/folders/qm/7s5b8sr96_99wh2x_9g1mwbr0000gn/T/phase31-acceptance-evidence.md \
  /var/folders/qm/7s5b8sr96_99wh2x_9g1mwbr0000gn/T/phase31-acceptance-evidence-logs
```

Actual: no matches.

### Task 4: Verification and Commit

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`
- Create: `tools/smoke/phase31-acceptance-evidence.sh`
- Create: `docs/superpowers/plans/2026-06-20-phase-31-acceptance-evidence-implementation-plan.md`

- [x] ⭐ **Step 1: Run final focused documentation coverage test**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH \
mvn -Dtest=DocumentationCoverageTest test
```

Actual: PASS, `DocumentationCoverageTest` 8 tests.

- [x] ⭐ **Step 2: Commit**

Run:

```bash
git add backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java tools/smoke/phase31-acceptance-evidence.sh docs/superpowers/plans/2026-06-20-phase-31-acceptance-evidence-implementation-plan.md
git commit -m "test: add acceptance evidence collector"
```

Actual: committed with message `test: add acceptance evidence collector`.
