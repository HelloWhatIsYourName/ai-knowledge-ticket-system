# V1 Demo Runbook

This runbook prepares a 10-15 minute defense demo for the AI knowledge-base ticket system. It assumes the backend, Oracle 23ai, Redis, and AI provider or mock provider are available.

## 1. Environment Startup

Start dependencies and backend:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn spring-boot:run
```

Expected startup evidence:

```text
Tomcat started on port 8080
Successfully validated 5 migrations
Schema "AI_TICKET" is up to date
```

Use the repeatable smoke script when a full backend check is needed:

```bash
BASE_URL=http://127.0.0.1:8080 tools/smoke/phase7-backend-smoke.sh
```

The script prints `token:redacted` and must not expose JWT values.

## 2. Login and RBAC

Demo accounts:

| Role | Username | Purpose |
| --- | --- | --- |
| 管理员 | `admin` | 知识库, 工单管理, 统计, 用户角色权限 |
| 普通用户 | `user` | RAG 问答, 转工单, 查看本人工单 |
| 坐席 | `agent` | 查看分配工单, 开始处理, 解决工单 |

Show:

1. `admin` login succeeds.
2. `user` login succeeds.
3. Anonymous admin request returns `401`.
4. `user` calling admin users or 统计 endpoint returns `403`.

This demonstrates RBAC and method-level permission enforcement.

## 3. Knowledge Base Preparation

As `admin`, create a text knowledge document for a stable demo question:

```text
Title: 密码重置 FAQ
Content: 用户忘记密码时，应在登录页选择忘记密码，完成身份验证后重置密码。如账号被锁定，可等待锁定时间结束或联系 IT 管理员解锁。
Category: 通用问题
```

Expected result:

```text
parseStatus = PARSE_SUCCESS
```

Then search the 知识库 as `user` with:

```text
忘记密码后应该如何重置？
```

Expected result: at least one chunk contains password reset guidance.

## 4. RAG Question

As `user`, ask:

```text
忘记密码后应该如何重置？
```

Show:

1. RAG returns an answer grounded in the knowledge document.
2. Response includes citations or related chunks.
3. The answer does not expose raw vector fields.

For thesis evaluation, compare this question against `docs/evaluation/rag-evaluation-set.json` and record 检索命中, 回答有用率, and whether transfer behavior is correct.

## 5. Manual Transfer to Ticket

Create a 工单 from the AI session when the user still needs manual confirmation.

Expected fields:

```text
status = PENDING_ASSIGN
sourceSessionId = AI session id
ticketNo is present
```

Show that the user can see the ticket in `my` tickets.

## 6. Admin Assignment

As `admin`, assign the ticket to `agent`.

Expected transition:

```text
PENDING_ASSIGN -> PENDING_PROCESS
```

Show that the admin can also open the 统计 overview:

```text
totalTickets
pendingTickets
processingTickets
resolvedTickets
knowledgeHitRate
```

This connects 工单 operations with management 统计.

## 7. Agent Processing

As `agent`:

1. Open assigned tickets.
2. Start processing.
3. Add an internal note if needed.
4. Resolve the ticket.

Expected transitions:

```text
PENDING_PROCESS -> PROCESSING -> RESOLVED
```

The flow log should record each state change.

## 8. User Feedback and Closure

As `user`:

1. Add a public reply if more information is needed.
2. Confirm close when resolved.

Expected transition:

```text
RESOLVED -> CLOSED
```

This demonstrates the user-side feedback loop currently implemented through `ticket_comment` and ticket workflow actions.

## 9. Admin Management Screens / APIs

Show these backend capabilities, even before frontend pages are connected:

| Area | Endpoint Family | Demo Point |
| --- | --- | --- |
| 用户管理 | `/api/admin/users` | list, enable, disable |
| 角色权限 | `/api/admin/roles`, `/api/admin/permissions` | role and permission visibility |
| 工单分类 | `/api/ticket-categories` | category list and management |
| 统计 | `/api/admin/statistics/*` | overview, category stats, hot questions |
| Redis | queue design and runtime dependency | document parsing queue and future hot ranking |

## 10. Closing Narrative

End the demo with this summary:

```text
The system implements the first-version backend loop: RBAC login, 知识库 ingestion, RAG answer with citations, transfer to 工单, agent workflow, user feedback, and admin 统计. Phase 7 adds repeatable smoke verification, a RAG evaluation set, and acceptance evidence for thesis writing. Frontend Vue3 + Element Plus + ECharts integration remains the next presentation layer.
```
