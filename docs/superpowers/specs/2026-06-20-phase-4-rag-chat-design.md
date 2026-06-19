# Phase 4 RAG Chat Design

## Goal

Phase 4 adds backend support for AI knowledge-base Q&A: retrieve relevant knowledge chunks, build a constrained RAG prompt, call the chat model, persist the session/message/citation trail, and expose both normal HTTP and SSE endpoints. Automatic ticket creation remains Phase 5; Phase 4 only records whether the answer should suggest manual transfer and why.

## Scope

Included:

- OpenAI-compatible chat provider implementation for DeepSeek-style chat completions.
- RAG orchestration service that calls the existing knowledge retrieval service.
- Prompt construction with source-numbered context and prompt-injection constraints.
- Oracle persistence for AI sessions, messages, citations, and answer quality metadata.
- HTTP ask endpoint for reliable backend verification.
- SSE ask endpoint that streams answer text while still reusing the same persistence model.
- RBAC protection through existing `ai:chat:ask` and `ai:chat:history:view` permissions.

Excluded:

- Creating tickets from unresolved conversations.
- Ticket workflow, assignment, comments, and status transitions.
- Frontend chat UI.
- Long-term memory, query rewriting, reranking, or prompt version management tables.

## Architecture

Phase 4 introduces an `ai.rag` module that depends on two stable abstractions:

1. `KnowledgeRetrievalService` for Top-K chunk recall.
2. `ChatClient` for model calls.

The controller layer remains thin. `RagChatService` owns the end-to-end ask flow:

1. Validate question and resolve optional existing session.
2. Retrieve knowledge chunks using Phase 3 vector retrieval.
3. Compute reliability from retrieval count, top similarity, and chat-model self assessment.
4. Build a strict prompt containing numbered source snippets.
5. Call the chat client.
6. Persist user message, assistant message, citations, and quality metadata.
7. Return answer, citation list, and transfer suggestion.

The HTTP and SSE endpoints share the same prompt builder, reliability policy, response DTOs, and persistence mapper. SSE is intentionally a delivery variant, not a separate business path.

## Extensibility Boundaries

### Chat Provider

`ChatClient` stays as the provider-facing interface. The first provider is `OpenAiCompatibleChatClient`, configured from `ai.chat.*`. It uses the `/chat/completions` contract and maps provider responses into `ChatResult`.

Future providers can be added by creating another `ChatClient` implementation and selecting it in configuration. RAG services do not depend on provider-specific request or response types.

### Prompting

`RagPromptBuilder` is a dedicated component. It receives the question, retrieved chunks, and recent conversation turns, and returns one prompt string. This keeps prompt changes out of controllers and persistence code.

The first version uses a constant prompt template. A later prompt-version table can replace the constant without changing the service contract.

### Reliability Policy

`RagAnswerPolicy` calculates:

- `canAnswer`
- `confidence`
- `transferSuggested`
- `transferReason`

The first policy combines retrieval evidence and chat self-assessment:

- no retrieved chunks means cannot answer
- top similarity below configured threshold means cannot answer
- model `canAnswer=false` lowers confidence and suggests transfer

Future scoring can add reranking, category filters, answer evaluation, or manual feedback without changing endpoint DTOs.

### Persistence

Oracle is the source of truth for AI history. Phase 4 creates:

- `ai_session`: one conversation owned by one user
- `ai_message`: user/assistant messages with model and reliability metadata
- `ai_message_citation`: source chunks cited by an assistant message

The schema stores IDs for `kb_document` and `kb_chunk` so Phase 5 tickets can link back to the exact AI session and cited knowledge evidence.

### Streaming

The SSE endpoint returns token events plus a final metadata event containing the same response shape as the HTTP endpoint. If a provider does not support real streaming yet, the adapter may stream the completed answer in deterministic chunks. This preserves the endpoint contract while allowing a later provider-level streaming implementation.

## API Design

### Ask

`POST /api/ai/chat/ask`

Permission: `ai:chat:ask`

Request:

```json
{
  "question": "忘记密码怎么处理？",
  "sessionId": 10,
  "categoryId": 1,
  "topK": 5,
  "minSimilarity": 0.7
}
```

`sessionId`, `categoryId`, `topK`, and `minSimilarity` are optional. If `sessionId` is missing, the backend creates a new session.

Response:

```json
{
  "sessionId": 10,
  "userMessageId": 20,
  "assistantMessageId": 21,
  "answer": "可以在登录页点击忘记密码...",
  "canAnswer": true,
  "confidence": 0.82,
  "transferSuggested": false,
  "transferReason": null,
  "citations": [
    {
      "citationIndex": 1,
      "chunkId": 6,
      "documentId": 3,
      "sourceTitle": "密码重置操作指南",
      "similarity": 0.79,
      "snippet": "当用户忘记密码时..."
    }
  ]
}
```

### Stream Ask

`GET /api/ai/chat/stream?question=...&sessionId=...&categoryId=...`

Permission: `ai:chat:ask`

Events:

- `token`: answer text fragment
- `metadata`: final JSON response with session/message IDs and citations
- `error`: sanitized error message

The first implementation may use completed-answer chunk streaming if the provider adapter does not yet implement provider-native streaming.

### History

`GET /api/ai/chat/sessions`

Permission: `ai:chat:history:view`

Returns the current user's sessions in reverse update order.

`GET /api/ai/chat/sessions/{sessionId}/messages`

Permission: `ai:chat:history:view`

Returns messages and citations for one owned session.

## Data Model

`ai_session`

- `id`
- `user_id`
- `title`
- `last_question`
- `transfer_suggested`
- `created_at`
- `updated_at`

`ai_message`

- `id`
- `session_id`
- `user_id`
- `role`: `USER` or `ASSISTANT`
- `content`
- `model_name`
- `can_answer`
- `confidence`
- `transfer_suggested`
- `transfer_reason`
- `created_at`

`ai_message_citation`

- `id`
- `message_id`
- `chunk_id`
- `document_id`
- `citation_index`
- `source_title`
- `snippet`
- `similarity`
- `created_at`

## Error Handling

- Missing or blank question returns validation errors.
- Unknown or non-owned `sessionId` returns 404.
- Chat provider missing API key fails fast with a clear sanitized message.
- Retrieval failures propagate as server errors and do not create assistant messages.
- Chat failures persist the user message and mark the session updated, but do not create a fake assistant answer.
- SSE emits a sanitized `error` event before completing.

## Testing

Unit tests cover:

- prompt builder source numbering and safety constraints
- reliability policy for low recall, high recall, and model self-refusal
- chat provider request/response mapping using mocked HTTP server behavior where practical
- RAG service persistence order and citation linking with fake mappers
- controller permission-independent request/response mapping

Live verification covers:

- admin/user login
- seed or reuse one Phase 3 knowledge document
- ask a password-reset question through HTTP
- verify answer, citations, session, messages, and no raw vectors
- verify SSE emits token and metadata events
- verify ordinary unauthorized history access is denied where applicable

## Phase 5 Handoff

Phase 5 ticket creation should consume Phase 4 outputs instead of re-running RAG:

- `sessionId`
- latest user question
- latest assistant answer
- citations
- `transferSuggested`
- `transferReason`

This keeps ticket creation auditable and avoids answer drift between the user-facing AI response and the ticket context shown to staff.
