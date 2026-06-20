# Phase 6 Admin Dashboard Verification

Date: 2026-06-20

## Scope

Phase 6 is a backend-first admin dashboard slice. It verifies:

- admin statistics overview, ticket category distribution, and hot question APIs
- ticket category listing API
- user, role, and permission administration APIs
- RBAC behavior for anonymous and ordinary user access

The repository does not yet contain a frontend project, so Vue3, Element Plus, and ECharts pages remain deferred until the backend contracts are stable.

## Automated Tests

Command:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo test
```

Result:

```text
Tests run: 92, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Focused regression coverage added for Oracle-compatible statistics SQL:

- `AdminStatisticsMapperXmlTest.overviewUsesCitationSimilarityInsteadOfMissingMessageColumn`
- `AdminStatisticsMapperXmlTest.hotQuestionsConvertClobContentBeforeGrouping`

These guard against referencing the nonexistent `ai_message.max_similarity` column and grouping raw Oracle `CLOB` values.

## Live Smoke

Backend restart command:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn spring-boot:run
```

Runtime evidence:

```text
Tomcat started on port 8080
Database: jdbc:oracle:thin:@localhost:1521/FREEPDB1 (Oracle 23.26)
Successfully validated 5 migrations
Schema "AI_TICKET" is up to date. No migration necessary.
```

Smoke command used `admin` and `user` logins with JWT output redacted. It called only read-only endpoints to avoid polluting live data.

Sanitized result:

```text
adminLogin 200 token:redacted
userLogin 200 token:redacted
overview 200 totalTickets,pendingTickets,processingTickets,resolvedTickets,closedTickets,averageResolveHours,knowledgeDocuments,aiQuestions
ticketCategoryStats 200 array(1)
hotQuestions 200 array(2)
ticketCategories 200 array(1)
users 200 array(4)
roles 200 array(4)
permissions 200 array(14)
anonymousOverview 401
userOverview 403
userSystemUsers 403
```

## Fix Notes

The first smoke attempt found two Oracle SQL issues in the admin statistics mapper:

- `GET /api/admin/statistics/overview` failed because the SQL referenced `ai_message.max_similarity`, which is not present in `V4__ai_rag_chat.sql`.
- `GET /api/admin/statistics/hot-questions` failed because the SQL grouped directly on `ai_message.content`, an Oracle `CLOB`.

The overview query now derives per-answer maximum similarity from `ai_message_citation.similarity` joined to assistant messages. The hot question query now uses `DBMS_LOB.SUBSTR(content, 1000, 1)` before trimming, normalizing, and grouping.

## Deferred Frontend Scope

Phase 6 exposes stable backend contracts for the future admin dashboard. The actual admin UI, ECharts visualizations, and Element Plus management tables remain in the next frontend integration slice.
