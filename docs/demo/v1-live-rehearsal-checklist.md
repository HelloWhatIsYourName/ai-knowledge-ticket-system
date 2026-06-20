# V1 Live Rehearsal Checklist

Date:
Operator:
Backend commit:
Frontend commit:
Provider mode: live SiliconFlow embeddings + live DeepSeek chat

Use this checklist for the final defense rehearsal. It is intentionally operational: every checked item should leave evidence that can be shown in the thesis defense or final review.

## 1. Preflight Environment

- [ ] Oracle 23ai is running and the backend can connect to the `AI_TICKET` schema.
- [ ] Redis is running and reachable by the backend.
- [ ] Backend starts on `http://127.0.0.1:8080` with Flyway migrations validated.
- [ ] Frontend starts through Vite and opens the app shell.
- [ ] `tools/smoke/phase19-demo-preflight.sh` prints `token:redacted` and returns zero before the rehearsal.
- [ ] No terminal output, screenshot, or recording exposes JWT values, provider API keys, database passwords, or user passwords.

## 2. Provider Readiness

- [ ] SiliconFlow embedding credentials are configured through environment or local configuration outside committed files.
- [ ] DeepSeek chat credentials are configured through environment or local configuration outside committed files.
- [ ] A knowledge upload or text ingestion produces parsed chunks without embedding dimension errors.
- [ ] A RAG question returns an answer and citations through the live provider path.
- [ ] If live provider calls fail, the demo switches to documented fallback talking points instead of editing code during the defense.

## 3. Demo Data Loaded

- [ ] Load or verify the corpus from `docs/demo/v1-demo-corpus.json`.
- [ ] Confirm the companion explanation in `docs/demo/v1-demo-corpus.md` matches the loaded knowledge documents.
- [ ] Open `/app/knowledge` as `admin`.
- [ ] Create one text document through the form.
- [ ] Upload one `.txt` or `.md` document through the file upload entry.
- [ ] Run retrieval test for `忘记密码后应该如何重置？` and capture at least one matching result.

## 4. Frontend Route Walkthrough

- [ ] `/app/demo` opens and presents the defense path.
- [ ] `/app/knowledge` supports text entry, file upload, document refresh, and retrieval test.
- [ ] `/app/ai/chat` streams an answer, shows citations, and keeps the transfer form visible.
- [ ] `/app/tickets/my` shows the current user's created tickets.
- [ ] `/app/tickets/assigned` shows tickets assigned to the `agent` account.
- [ ] `/app/admin/dashboard` shows total, pending, processing, resolved, hit-rate, category, and hot-question statistics.
- [ ] `/app/system` shows users, roles, permissions, and role assignment controls.

## 5. RAG Evidence Capture

- [ ] Ask `忘记密码后应该如何重置？` in `/app/ai/chat`.
- [ ] Capture answer text, citation cards, and whether the answer streamed progressively.
- [ ] Record the result in `docs/evaluation/rag-live-evaluation-report.md`.
- [ ] Run enough cases from `docs/evaluation/rag-evaluation-set.json` to support thesis discussion.
- [ ] Record 检索命中率, 回答有用率, 误转工单率, and 应转未转率.

## 6. Ticket Workflow Evidence Capture

- [ ] Convert an unresolved AI session into a ticket.
- [ ] Confirm the new ticket appears in `/app/tickets/my`.
- [ ] Assign the ticket as `admin`.
- [ ] Confirm the assigned ticket appears in `/app/tickets/assigned`.
- [ ] Start processing as `agent`.
- [ ] Add an internal note.
- [ ] Resolve the ticket.
- [ ] Add a public reply as `user`.
- [ ] Confirm close as `user`.
- [ ] Capture the flow log showing status transitions.

## 7. Admin Evidence Capture

- [ ] Open `/app/admin/dashboard` after at least one ticket workflow.
- [ ] Capture overview statistics and category distribution.
- [ ] Open `/app/system`.
- [ ] Capture user list, role list, permission list, and one role assignment view.
- [ ] Demonstrate that a non-admin account cannot access admin-only APIs.

## 8. Failure Fallbacks

- [ ] If `/api/ai/chat/stream` fails, demonstrate that `/api/ai/chat/ask` still supports the normal answer path.
- [ ] If the provider is rate-limited, show the existing evidence report and explain provider dependency boundaries.
- [ ] If the demo corpus is incomplete, mark retrieval misses as corpus gaps instead of model failures.
- [ ] If login state expires, use `/login?redirect=...` and show `/api/auth/me` session restoration after login.

## 9. Final Defense Evidence Pack

- [ ] `docs/acceptance/v1-acceptance-checklist.md`
- [ ] `docs/demo/v1-demo-runbook.md`
- [ ] `docs/demo/v1-live-rehearsal-checklist.md`
- [ ] `docs/demo/v1-demo-corpus.json`
- [ ] `docs/evaluation/rag-evaluation-set.json`
- [ ] `docs/evaluation/rag-live-evaluation-report.md`
- [ ] Latest backend `mvn test` output.
- [ ] Latest frontend `npm run test` output.
- [ ] Latest frontend `npm run build` output.
