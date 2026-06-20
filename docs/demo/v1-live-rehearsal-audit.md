# V1 Live Rehearsal Audit

Date: 2026-06-20
Worktree: `knowledge-live-verification`
Audit script: `tools/smoke/phase21-rehearsal-audit.sh`
Secret handling sentinel: `token:redacted`

This audit records the current machine state before a full live-provider rehearsal. It intentionally distinguishes implemented product readiness from local environment prerequisites.

## Current Result

| Area | Observed Status | Evidence | Next Action |
| --- | --- | --- | --- |
| Docker services | PASS | `docker compose ps` shows Oracle 23ai and Redis containers running and healthy. | Keep containers running for rehearsal. |
| Oracle 23ai | PASS | `ai-ticket-oracle` is healthy on port `1521`. | Start backend against `jdbc:oracle:thin:@localhost:1521/FREEPDB1`. |
| Redis | PASS | `ai-ticket-redis` is healthy on port `6379`. | Keep Redis available for backend queue/cache paths. |
| Backend | PASS | `http://127.0.0.1:8080/api/auth/me` returned `401`, proving the backend is reachable and authentication is enforced. | Keep backend running for browser walkthrough. |
| Frontend | PASS | `http://127.0.0.1:5174/` returned `200`. | Open the app in the browser for visual walkthrough. |
| AI_EMBEDDING_API_KEY | PASS | Environment was sourced from local secret file; value was not printed. | Keep the secret outside committed files. |
| AI_CHAT_API_KEY | PASS | Environment was sourced from local secret file; value was not printed. | Rotate the key after defense because it was pasted into chat once. |
| Phase 19 preflight | PASS | `phase21-rehearsal-audit.sh` invoked `tools/smoke/phase19-demo-preflight.sh`; login, auth, knowledge search, RAG ask, RAG stream, and admin overview returned expected statuses. | Continue with demo corpus loading and RAG metric recording. |

## Commands Used

```bash
docker compose ps
curl -sS -o /tmp/ai-ticket-backend-health.txt -w '%{http_code}' http://127.0.0.1:8080/api/auth/me
curl -sS -o /tmp/ai-ticket-frontend-health.txt -w '%{http_code}' http://127.0.0.1:5174/
rg -o "^[A-Za-z0-9_]+=" /private/tmp/ai-ticket-secrets/siliconflow.env
tools/smoke/phase21-rehearsal-audit.sh
```

## Re-run Procedure

1. Source provider secrets without printing values:

```bash
set -a
source /private/tmp/ai-ticket-secrets/siliconflow.env
source /private/tmp/ai-ticket-secrets/deepseek.env
set +a
```

2. Start backend:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn spring-boot:run
```

3. Start frontend:

```bash
cd frontend
npm run dev -- --host 127.0.0.1 --port 5174
```

4. Run the audit and then the Phase 19 preflight:

```bash
tools/smoke/phase21-rehearsal-audit.sh
tools/smoke/phase19-demo-preflight.sh
```

## Latest Sanitized Audit Output

```text
phase21RehearsalAudit start token:redacted
Backend /api/auth/me reachable 401
Frontend reachable 200
AI_EMBEDDING_API_KEY present value:redacted
AI_CHAT_API_KEY present value:redacted
adminLogin 200 token:redacted
userLogin 200 token:redacted
authMe 200 user,roles,permissions,menus
knowledgeSearch 200 array(1)
ragAsk 200 sessionId,userMessageId,assistantMessageId,answer,canAnswer,confidence,transferSuggested,transferReason,citations
ragStream 200 sse
adminOverview 200 totalTickets,pendingTickets,processingTickets,resolvedTickets,closedTickets,averageResolveHours,knowledgeDocuments,aiQuestions,knowledgeHitRate
phase19Preflight complete token:redacted
phase21RehearsalAudit complete token:redacted
```

## Interpretation

The local live preflight is now PASS. Oracle 23ai, Redis, backend, frontend, SiliconFlow embedding configuration, DeepSeek chat configuration, RAG ask, SSE stream, and admin overview are reachable through the repeatable smoke path. No provider key values, JWT values, database passwords, or user passwords were printed.
