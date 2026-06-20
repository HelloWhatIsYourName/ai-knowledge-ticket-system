# Phase 30 Frontend Dev Smoke Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a repeatable frontend development smoke script so browser-level regressions, especially Vite `/api` proxy login failures, are caught before claiming the app is usable locally.

**Architecture:** Keep the smoke script outside the frontend package under `tools/smoke/` so it can validate the running dev server as a black box. The script checks Vite HTML fallback routes, performs same-origin login through the frontend `/api` proxy, and verifies the returned token against `/api/auth/me` without printing secrets.

**Tech Stack:** Bash, curl, Node.js JSON parsing, Vite dev server, Spring Boot auth API.

---

### Task 1: Add Regression Coverage

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`

- [x] ⭐ **Step 1: Add a failing documentation coverage test**

Add coverage for `tools/smoke/phase30-frontend-dev-smoke.sh`, requiring:

- `FRONTEND_BASE_URL`
- default `http://127.0.0.1:5174`
- proxied `/api/auth/login`
- proxied `/api/auth/me`
- core routes `/login`, `/app`, `/app/ai/chat`, `/app/tickets/my`, `/app/admin/dashboard`
- `token:redacted`

- [x] ⭐ **Step 2: Verify RED**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH \
mvn -Dtest=DocumentationCoverageTest test
```

Actual: FAIL because `../tools/smoke/phase30-frontend-dev-smoke.sh` did not exist.

### Task 2: Add Frontend Dev Smoke Script

**Files:**
- Create: `tools/smoke/phase30-frontend-dev-smoke.sh`

- [x] ⭐ **Step 1: Implement route checks**

Check that the Vite dev server returns HTML app shell content for:

- `/`
- `/login`
- `/app`
- `/app/ai/chat`
- `/app/tickets/my`
- `/app/admin/dashboard`

- [x] ⭐ **Step 2: Implement proxied API checks**

Through `FRONTEND_BASE_URL`, check:

- `POST /api/auth/login` returns 200 and `success=true`
- token is parsed but never printed
- `GET /api/auth/me` returns 200 and the expected username

- [x] ⭐ **Step 3: Verify focused GREEN**

Run:

```bash
bash -n tools/smoke/phase30-frontend-dev-smoke.sh
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH \
mvn -Dtest=DocumentationCoverageTest test
```

Actual: PASS, `DocumentationCoverageTest` 7 tests.

### Task 3: Run Live Frontend Smoke

**Files:**
- No code changes.

- [x] ⭐ **Step 1: Confirm backend and frontend dev services are running**

Expected:

- Backend: `http://127.0.0.1:8080`
- Frontend: `http://127.0.0.1:5174/`

- [x] ⭐ **Step 2: Run frontend dev smoke**

Run:

```bash
tools/smoke/phase30-frontend-dev-smoke.sh
```

Expected: PASS through HTML routes, proxied login, token-redacted output, and `/api/auth/me`.

Actual: PASS through `/`, `/login`, `/app`, `/app/ai/chat`, `/app/tickets/my`, `/app/admin/dashboard`, proxied admin login, token-redacted output, and `/api/auth/me`.

- [x] ⭐ **Step 3: Verify browser login path**

Use Chrome against:

```text
http://127.0.0.1:5174/login
```

Actual: `admin / Admin_123456` logs in and redirects to `/app/knowledge`.

### Task 4: Verification and Commit

**Files:**
- Modify: `backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java`
- Create: `tools/smoke/phase30-frontend-dev-smoke.sh`
- Create: `docs/superpowers/plans/2026-06-20-phase-30-frontend-dev-smoke-implementation-plan.md`

- [x] ⭐ **Step 1: Run frontend tests**

Run:

```bash
cd frontend
npm run test
```

Actual: PASS, 20 files, 41 tests.

- [x] ⭐ **Step 2: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Actual: PASS, `vue-tsc --noEmit` and `vite build`.

- [x] ⭐ **Step 3: Run backend focused documentation coverage test**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH \
mvn -Dtest=DocumentationCoverageTest test
```

Actual: PASS, `DocumentationCoverageTest` 7 tests.

- [x] ⭐ **Step 4: Commit**

Run:

```bash
git add backend/src/test/java/com/example/aiticket/docs/DocumentationCoverageTest.java tools/smoke/phase30-frontend-dev-smoke.sh docs/superpowers/plans/2026-06-20-phase-30-frontend-dev-smoke-implementation-plan.md
git commit -m "test: add frontend dev smoke script"
```

Actual: committed with message `test: add frontend dev smoke script`.
