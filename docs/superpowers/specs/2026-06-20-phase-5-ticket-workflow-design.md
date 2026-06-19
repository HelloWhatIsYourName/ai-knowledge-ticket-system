# Phase 5 Ticket Workflow Design

## Goal

Phase 5 starts the ticket collaboration backend. It turns unresolved AI conversations into traceable tickets, stores workflow history, and exposes a small set of APIs that user, agent, and admin pages can build on.

## Scope

Included in the first Phase 5 slice:

- Oracle schema for ticket categories, tickets, comments, and workflow logs.
- Domain records and enums for ticket status, priority, action, source, and summary views.
- MyBatis mapper for ticket creation, workflow logs, owned-ticket listing, assigned-ticket listing, management listing, and ticket detail loading.
- `TicketWorkflowService` as the only place that creates tickets or mutates ticket status.
- An explicit manual assignment boundary so later automatic assignment strategies can be added without changing controller contracts.
- REST endpoints for creating a ticket from an AI session, listing own/assigned/managed tickets, viewing ticket detail, assigning, starting processing, resolving, reopening, closing, and confirming closure.
- RBAC through existing `ticket:create`, `ticket:view:own`, `ticket:assign`, `ticket:process`, and `ticket:manage` permissions.

Excluded from this slice:

- Frontend ticket pages.
- Complex SLA escalation.
- Multi-department approval flows.
- Automatic assignee selection.
- Dashboard statistics.

## Architecture

The module lives under `com.example.aiticket.ticket`.

```text
ticket.web
  -> ticket.service.TicketWorkflowService
    -> ticket.mapper.TicketMapper
    -> ai.rag.mapper.AiChatMapper
```

Controllers stay thin and permission-focused. `TicketWorkflowService` owns business rules:

1. Create tickets from owned AI sessions and optional assistant message context.
2. Write a `ticket_flow_log` row for every creation and status transition.
3. Enforce status transitions in one place.
4. Keep user-owned and agent-assigned access separate from admin management access.

Phase 4 RAG is not re-run during ticket creation. Phase 5 consumes persisted AI session/message/citation data so ticket creation is deterministic and auditable.

## Data Model

`ticket_category`

- `id`
- `name`
- `parent_id`
- `sort_order`
- `enabled`
- `created_at`
- `updated_at`

`ticket`

- `id`
- `ticket_no`
- `title`
- `description`
- `status`
- `priority`
- `ai_priority_suggestion`
- `category_id`
- `department_id`
- `creator_id`
- `assignee_id`
- `source`
- `source_session_id`
- `source_message_id`
- `ai_summary`
- `ai_suggestion`
- `transfer_reason`
- `deadline_at`
- `first_resolved_at`
- `closed_at`
- `reopen_count`
- `deleted`
- `created_at`
- `updated_at`

`ticket_flow_log`

- `id`
- `ticket_id`
- `from_status`
- `to_status`
- `action`
- `operator_id`
- `operator_role`
- `comment`
- `created_at`

`ticket_comment`

- `id`
- `ticket_id`
- `author_id`
- `comment_type`
- `content`
- `internal`
- `created_at`

## Workflow

Initial ticket creation creates status `PENDING_ASSIGN` and flow action `CREATE`.

Allowed first-version transitions:

| From | Action | Role | To |
| --- | --- | --- | --- |
| `PENDING_ASSIGN` | `ASSIGN` | Admin | `PENDING_PROCESS` |
| `PENDING_ASSIGN` | `CLOSE` | Admin | `CLOSED` |
| `PENDING_PROCESS` | `START_PROCESS` | Agent | `PROCESSING` |
| `PENDING_PROCESS` | `CLOSE` | Admin | `CLOSED` |
| `PROCESSING` | `RESOLVE` | Agent | `RESOLVED` |
| `PROCESSING` | `CLOSE` | Admin | `CLOSED` |
| `RESOLVED` | `REOPEN` | User | `PROCESSING` |
| `RESOLVED` | `CONFIRM_CLOSE` | User | `CLOSED` |

Any unsupported transition throws a domain exception and does not update the ticket row.

## API Design

`POST /api/tickets/from-ai-session`

Permission: `ticket:create`

Creates a ticket from an owned AI session.

```json
{
  "sessionId": 7,
  "assistantMessageId": 14,
  "title": "忘记密码无法处理",
  "description": "用户仍需要人工确认账号绑定信息",
  "categoryId": 1,
  "priority": "NORMAL",
  "transferReason": "AI 建议转人工"
}
```

`GET /api/tickets/my`

Permission: `ticket:view:own`

Lists tickets created by the current user.

`GET /api/tickets/assigned`

Permission: `ticket:process`

Lists tickets assigned to the current agent.

`GET /api/tickets/manage`

Permission: `ticket:manage`

Lists non-deleted tickets for administrators.

`GET /api/tickets/{ticketId}`

Permission: `ticket:view:own`, `ticket:process`, or `ticket:manage`; service enforces ownership/assignment unless caller has manage access.

`POST /api/tickets/{ticketId}/assign`

Permission: `ticket:assign`

`POST /api/tickets/{ticketId}/start`

Permission: `ticket:process`

`POST /api/tickets/{ticketId}/resolve`

Permission: `ticket:process`

`POST /api/tickets/{ticketId}/reopen`

Permission: `ticket:view:own`

`POST /api/tickets/{ticketId}/confirm-close`

Permission: `ticket:view:own`

`POST /api/tickets/{ticketId}/close`

Permission: `ticket:manage`

## Extensibility

Assignment is a service boundary, not controller logic. The first implementation accepts a manual assignee ID. Later strategies can implement:

- category-based routing
- agent load balancing
- skill-based assignment
- department-aware routing

Status transitions are also centralized. SLA, approval, and notification hooks can subscribe around `TicketWorkflowService` without scattering workflow rules across controllers.

## Testing

Tests should cover:

- V5 migration defines all ticket tables, constraints, and indexes.
- mapper XML declares create/list/detail/transition statements.
- service creates a ticket from an owned AI session and logs `CREATE`.
- service rejects non-owned AI sessions.
- service allows valid transitions and rejects invalid transitions.
- controller methods retain expected permission annotations and response mapping.
