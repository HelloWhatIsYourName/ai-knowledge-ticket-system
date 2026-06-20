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
| Backend | BLOCKED | `http://127.0.0.1:8080/api/auth/me` was not reachable during audit. | Start backend with Java 21 and provider environment variables. |
| Frontend | BLOCKED | `http://127.0.0.1:5174/` was not reachable during audit. | Start Vite frontend before browser walkthrough. |
| AI_EMBEDDING_API_KEY | PARTIAL | `/private/tmp/ai-ticket-secrets/siliconflow.env` exists and defines `AI_EMBEDDING_API_KEY`; value was not printed. | Source the file before backend startup. |
| AI_CHAT_API_KEY | BLOCKED | No `AI_CHAT_API_KEY` was present in the current environment. | Configure DeepSeek chat key outside committed files before full live RAG rehearsal. |
| Phase 19 preflight | BLOCKED | `tools/smoke/phase19-demo-preflight.sh` requires backend reachability and provider keys. | Re-run after backend, frontend, and chat key are ready. |

## Commands Used

```bash
docker compose ps
curl -sS -o /tmp/ai-ticket-backend-health.txt -w '%{http_code}' http://127.0.0.1:8080/api/auth/me
curl -sS -o /tmp/ai-ticket-frontend-health.txt -w '%{http_code}' http://127.0.0.1:5174/
rg -o "^[A-Za-z0-9_]+=" /private/tmp/ai-ticket-secrets/siliconflow.env
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

## Interpretation

The product implementation is not blocked by this audit. The current local live rehearsal is BLOCKED by runtime prerequisites: backend not running, frontend not running, and `AI_CHAT_API_KEY` missing. No provider key values, JWT values, database passwords, or user passwords were printed.
