# Phase 22 Live Preflight Pass Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Convert live rehearsal readiness from blocked to pass after configuring the DeepSeek chat key and starting backend/frontend services.

**Architecture:** Keep the fix operational and evidence-focused. Repair the local Flyway schema history instead of deleting Oracle data, start backend and frontend with provider secrets sourced from `/private/tmp/ai-ticket-secrets`, correct smoke scripts to match RBAC and SSE method contracts, then record sanitized preflight evidence.

**Tech Stack:** Bash, Maven/Flyway, Spring Boot, Vite, Markdown, JUnit 5.

---

## Task 1: Secret and Runtime Setup ⭐

- [x] **Step 1: Store DeepSeek key outside Git**

Created `/private/tmp/ai-ticket-secrets/deepseek.env` with `AI_CHAT_API_KEY` and mode `600`. The key value is not committed and should be rotated after defense because it was pasted into chat once.

- [x] **Step 2: Confirm secret files by variable name only**

Ran `rg -o '^[A-Za-z0-9_]+=' /private/tmp/ai-ticket-secrets/deepseek.env /private/tmp/ai-ticket-secrets/siliconflow.env`.

- [x] **Step 3: Repair local Flyway schema history**

Ran Flyway repair for local Oracle because V2 had a historical checksum mismatch after route seed alignment. Repair updated schema history without dropping data.

- [x] **Step 4: Start backend**

Started backend with Java 21 and sourced provider env files. Flyway validated 6 migrations, migrated schema to v6, and Tomcat started on port 8080.

- [x] **Step 5: Start frontend**

Started Vite on `http://127.0.0.1:5174/`.

## Task 2: Smoke Script Alignment ⭐

- [x] **Step 1: Add failing docs coverage assertion**

Updated `DocumentationCoverageTest` to assert direct `/api/kb/search` smoke checks use `$ADMIN_TOKEN`, matching `knowledge:document:view` RBAC.

- [x] **Step 2: Verify RED**

Ran `mvn -Dtest=DocumentationCoverageTest test`; it failed because both Phase 7 and Phase 19 scripts used `$USER_TOKEN` for knowledge search.

- [x] **Step 3: Fix Phase 7 and Phase 19 scripts**

Changed `knowledgeSearch` to use `$ADMIN_TOKEN`. Also changed Phase 19 stream check from POST to GET with `--data-urlencode`, matching `RagChatController#stream`.

- [x] **Step 4: Verify GREEN**

Ran `bash -n` for both smoke scripts and `mvn -Dtest=DocumentationCoverageTest test`; all passed.

## Task 3: Live Preflight Evidence ⭐

- [x] **Step 1: Run live audit**

Ran:

```bash
set -a
source /private/tmp/ai-ticket-secrets/siliconflow.env
source /private/tmp/ai-ticket-secrets/deepseek.env
set +a
tools/smoke/phase21-rehearsal-audit.sh
```

Result: PASS. Backend, frontend, both provider keys, login, auth, knowledge search, RAG ask, RAG stream, and admin overview all returned expected statuses.

- [x] **Step 2: Update audit report**

Updated `docs/demo/v1-live-rehearsal-audit.md` with sanitized PASS output and no secret values.

- [x] **Step 3: Commit Phase 22 slice**

Commit this phase after full verification.
