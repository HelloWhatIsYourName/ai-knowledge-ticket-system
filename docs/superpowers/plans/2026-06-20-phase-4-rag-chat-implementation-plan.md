# Phase 4 RAG Chat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate.

**Goal:** Implement Phase 4 backend RAG chat with model calls, answer citations, persistence, HTTP/SSE endpoints, and history APIs.

**Architecture:** Keep RAG orchestration separate from provider integration, prompt building, answer policy, and Oracle persistence. HTTP and SSE endpoints call the same service path so future provider-native streaming, prompt versioning, ticket creation, and feedback scoring can be added without changing endpoint contracts.

**Tech Stack:** Java 21, Spring Boot 3, Spring Security method authorization, MyBatis XML, Flyway Oracle migrations, RestClient, SSE `SseEmitter`, JUnit 5.

---

## Scope

This plan implements:

1. `ai_session`, `ai_message`, and `ai_message_citation` Oracle tables.
2. OpenAI-compatible `ChatClient` implementation for `ai.chat.*`.
3. RAG prompt builder with numbered citations and prompt-injection guardrails.
4. Reliability policy for `canAnswer`, confidence, transfer suggestion, and transfer reason.
5. `RagChatService` that retrieves knowledge, calls chat, persists messages/citations, and returns a stable DTO.
6. HTTP ask endpoint, SSE ask endpoint, session list endpoint, and session messages endpoint.
7. Unit tests and live verification steps.

This plan intentionally does not implement:

1. Automatic ticket creation.
2. Ticket workflow tables or APIs.
3. Frontend chat pages.
4. Provider-native token streaming; the first SSE implementation may stream deterministic chunks from the completed model answer.
5. Prompt version database management.

## File Structure

```text
backend/
  src/main/java/com/example/aiticket/ai/chat/OpenAiCompatibleChatClient.java
  src/main/java/com/example/aiticket/ai/rag/domain/AiSession.java
  src/main/java/com/example/aiticket/ai/rag/domain/AiMessage.java
  src/main/java/com/example/aiticket/ai/rag/domain/AiMessageCitation.java
  src/main/java/com/example/aiticket/ai/rag/domain/AiMessageRole.java
  src/main/java/com/example/aiticket/ai/rag/domain/RagAnswer.java
  src/main/java/com/example/aiticket/ai/rag/domain/RagCitation.java
  src/main/java/com/example/aiticket/ai/rag/domain/RagPrompt.java
  src/main/java/com/example/aiticket/ai/rag/domain/RagPolicyDecision.java
  src/main/java/com/example/aiticket/ai/rag/mapper/AiChatMapper.java
  src/main/java/com/example/aiticket/ai/rag/prompt/RagPromptBuilder.java
  src/main/java/com/example/aiticket/ai/rag/service/RagAnswerPolicy.java
  src/main/java/com/example/aiticket/ai/rag/service/RagChatService.java
  src/main/java/com/example/aiticket/ai/rag/web/AskQuestionRequest.java
  src/main/java/com/example/aiticket/ai/rag/web/RagAnswerResponse.java
  src/main/java/com/example/aiticket/ai/rag/web/RagCitationResponse.java
  src/main/java/com/example/aiticket/ai/rag/web/AiSessionResponse.java
  src/main/java/com/example/aiticket/ai/rag/web/AiMessageResponse.java
  src/main/java/com/example/aiticket/ai/rag/web/RagChatController.java
  src/main/resources/db/migration/V4__ai_rag_chat.sql
  src/main/resources/mapper/AiChatMapper.xml
  src/test/java/com/example/aiticket/ai/chat/OpenAiCompatibleChatClientTest.java
  src/test/java/com/example/aiticket/ai/rag/prompt/RagPromptBuilderTest.java
  src/test/java/com/example/aiticket/ai/rag/service/RagAnswerPolicyTest.java
  src/test/java/com/example/aiticket/ai/rag/service/RagChatServiceTest.java
  src/test/java/com/example/aiticket/ai/rag/web/RagChatControllerTest.java
docs/spikes/phase-4-rag-chat.md
```

## Task 1: Schema and Domain Model ⭐

**Files:**
- Create: `backend/src/main/resources/db/migration/V4__ai_rag_chat.sql`
- Create: `backend/src/main/java/com/example/aiticket/ai/rag/domain/*.java`
- Create: `backend/src/main/java/com/example/aiticket/ai/rag/mapper/AiChatMapper.java`
- Create: `backend/src/main/resources/mapper/AiChatMapper.xml`

- [x] **Step 1: Add V4 migration**

Create `ai_session`, `ai_message`, and `ai_message_citation` with foreign keys to `sys_user`, `kb_document`, and `kb_chunk`. Use `NUMBER(1)` for booleans and check constraints for message roles.

- [x] **Step 2: Add focused domain records**

Create records for session, message, citation, response-level answer, response-level citation, prompt, policy decision, and role enum. Keep provider-specific JSON classes out of this package.

- [x] **Step 3: Add mapper interface and XML**

Implement insert session, update session summary, insert message, insert citation, list owned sessions, get owned session, list session messages, and list message citations.

- [x] **Step 4: Verify mapper XML loads**

Run:

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo -Dtest=KnowledgeMapperXmlTest test
```

- [x] **Step 5: Commit Task 1**

```bash
git add backend/src/main/resources/db/migration/V4__ai_rag_chat.sql backend/src/main/java/com/example/aiticket/ai/rag/domain backend/src/main/java/com/example/aiticket/ai/rag/mapper backend/src/main/resources/mapper/AiChatMapper.xml
git commit -m "feat: add rag chat persistence schema"
```

## Task 2: Chat Provider Adapter ⭐

**Files:**
- Create: `backend/src/main/java/com/example/aiticket/ai/chat/OpenAiCompatibleChatClient.java`
- Test: `backend/src/test/java/com/example/aiticket/ai/chat/OpenAiCompatibleChatClientTest.java`

- [x] **Step 1: Write provider adapter tests**

Cover request URL `/chat/completions`, authorization header, model field, message payload, response content extraction, blank prompt rejection, missing response rejection, and optional structured self-assessment parsing when the model returns JSON.

- [x] **Step 2: Implement `OpenAiCompatibleChatClient`**

Use `RestClient`, `AiProviderProperties.getChat()`, and map the first choice message content to `ChatResult`.

- [x] **Step 3: Keep streaming extensible**

Implement `streamChat(String prompt)` as completed-answer chunk streaming through `SseEmitter`; keep this method in the adapter so provider-native streaming can replace only adapter internals later.

- [x] **Step 4: Run focused tests**

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo -Dtest=OpenAiCompatibleChatClientTest test
```

- [x] **Step 5: Commit Task 2**

```bash
git add backend/src/main/java/com/example/aiticket/ai/chat/OpenAiCompatibleChatClient.java backend/src/test/java/com/example/aiticket/ai/chat/OpenAiCompatibleChatClientTest.java
git commit -m "feat: add openai compatible chat client"
```

## Task 3: Prompt Builder and Answer Policy ⭐

**Files:**
- Create: `backend/src/main/java/com/example/aiticket/ai/rag/prompt/RagPromptBuilder.java`
- Create: `backend/src/main/java/com/example/aiticket/ai/rag/service/RagAnswerPolicy.java`
- Test: `backend/src/test/java/com/example/aiticket/ai/rag/prompt/RagPromptBuilderTest.java`
- Test: `backend/src/test/java/com/example/aiticket/ai/rag/service/RagAnswerPolicyTest.java`

- [x] **Step 1: Write prompt builder tests**

Assert numbered sources, source metadata, user question, and safety constraints are present. Assert blank retrieval still produces a prompt that instructs the model to say it cannot confirm.

- [x] **Step 2: Implement prompt builder**

Return `RagPrompt` with the full prompt and citation drafts derived from `KnowledgeSearchResult`.

- [x] **Step 3: Write policy tests**

Cover no recall, top similarity below threshold, high-confidence answer, and model self-refusal.

- [x] **Step 4: Implement answer policy**

Calculate confidence conservatively from retrieval similarity and model confidence. Set transfer suggestions when recall is empty/weak or model refuses.

- [x] **Step 5: Run focused tests**

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo -Dtest=RagPromptBuilderTest,RagAnswerPolicyTest test
```

- [x] **Step 6: Commit Task 3**

```bash
git add backend/src/main/java/com/example/aiticket/ai/rag/prompt backend/src/main/java/com/example/aiticket/ai/rag/service/RagAnswerPolicy.java backend/src/test/java/com/example/aiticket/ai/rag/prompt backend/src/test/java/com/example/aiticket/ai/rag/service/RagAnswerPolicyTest.java
git commit -m "feat: add rag prompt and answer policy"
```

## Task 4: RAG Chat Service ⭐

**Files:**
- Create: `backend/src/main/java/com/example/aiticket/ai/rag/service/RagChatService.java`
- Test: `backend/src/test/java/com/example/aiticket/ai/rag/service/RagChatServiceTest.java`

- [x] **Step 1: Write service tests with fake collaborators**

Assert the service creates a session when missing, verifies session ownership when provided, persists user message before chat call, persists assistant message after chat call, persists citations linked to assistant message, and does not persist fake assistant messages when chat fails.

- [x] **Step 2: Implement `ask(...)`**

Use `KnowledgeRetrievalService`, `RagPromptBuilder`, `ChatClient`, `RagAnswerPolicy`, and `AiChatMapper`. Return `RagAnswer`.

- [x] **Step 3: Implement history methods**

List owned sessions and list messages/citations for an owned session. Do not expose another user's session.

- [x] **Step 4: Run focused tests**

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo -Dtest=RagChatServiceTest test
```

- [x] **Step 5: Commit Task 4**

```bash
git add backend/src/main/java/com/example/aiticket/ai/rag/service/RagChatService.java backend/src/test/java/com/example/aiticket/ai/rag/service/RagChatServiceTest.java
git commit -m "feat: add rag chat orchestration service"
```

## Task 5: REST and SSE API ⭐

**Files:**
- Create: `backend/src/main/java/com/example/aiticket/ai/rag/web/*.java`
- Test: `backend/src/test/java/com/example/aiticket/ai/rag/web/RagChatControllerTest.java`

- [x] **Step 1: Write controller tests**

Cover HTTP ask response mapping, SSE token/metadata behavior with a fake service, session list mapping, message list mapping, and validation for blank questions.

- [x] **Step 2: Implement DTOs**

Create request/response records with validation annotations and no vector fields.

- [x] **Step 3: Implement controller**

Add:

```text
POST /api/ai/chat/ask
GET  /api/ai/chat/stream
GET  /api/ai/chat/sessions
GET  /api/ai/chat/sessions/{sessionId}/messages
```

Use `@PreAuthorize("hasAuthority('ai:chat:ask')")` and `@PreAuthorize("hasAuthority('ai:chat:history:view')")`.

- [x] **Step 4: Run focused tests**

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo -Dtest=RagChatControllerTest test
```

- [x] **Step 5: Commit Task 5**

```bash
git add backend/src/main/java/com/example/aiticket/ai/rag/web backend/src/test/java/com/example/aiticket/ai/rag/web/RagChatControllerTest.java
git commit -m "feat: expose rag chat api"
```

## Task 6: Full Verification and Live Spike

**Files:**
- Create: `docs/spikes/phase-4-rag-chat.md`
- Modify: `docs/superpowers/plans/2026-06-20-phase-4-rag-chat-implementation-plan.md`

- [ ] **Step 1: Run all unit tests**

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin:$PATH mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo test
```

- [ ] **Step 2: Run local backend with chat and embedding keys**

Load `/private/tmp/ai-ticket-secrets/siliconflow.env` and the local chat key file if present. Do not print keys or JWTs.

- [ ] **Step 3: Verify HTTP ask**

Login as `admin`, create or reuse the password reset knowledge document, call `POST /api/ai/chat/ask`, and verify response contains answer text, citations, session ID, message IDs, and no raw vector fields.

- [ ] **Step 4: Verify SSE ask**

Call `GET /api/ai/chat/stream` with an admin token and verify at least one `token` event and one `metadata` event.

- [ ] **Step 5: Verify history**

Call session and message history endpoints as the owning user and confirm persisted citations are returned.

- [ ] **Step 6: Verify RBAC**

Call chat/history endpoints without a token and confirm `401`. Use an account without required permission when available and confirm `403`.

- [ ] **Step 7: Record spike report**

Write sanitized evidence to `docs/spikes/phase-4-rag-chat.md`.

- [ ] **Step 8: Final diff and plan marking**

Run `git diff --check`, inspect scope, mark completed tasks with checkboxes and stars.

- [ ] **Step 9: Commit Task 6**

```bash
git add docs/spikes/phase-4-rag-chat.md docs/superpowers/plans/2026-06-20-phase-4-rag-chat-implementation-plan.md
git commit -m "docs: verify phase 4 rag chat"
```

## Final Report

Report:

- commits created
- unit test command and result
- live HTTP/SSE verification result
- remaining Phase 5 scope
- any unavailable live provider keys or skipped checks
