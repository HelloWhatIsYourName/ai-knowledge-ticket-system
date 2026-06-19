# Phase 4 RAG Chat Spike

Date: 2026-06-20

## Scope

This verification covers the Phase 4 backend RAG chat path:

- OpenAI-compatible chat adapter contract
- RAG prompt construction with numbered citations
- conservative answer policy and transfer suggestion fields
- Oracle persistence for sessions, messages, and citations
- HTTP ask, SSE ask, session history, and message history APIs
- RBAC behavior for chat-enabled and non-chat users

Automatic ticket creation, ticket workflow APIs, frontend chat pages, and provider-native streaming remain Phase 5 or later scope.

## Environment

- Branch/worktree: `knowledge-live-verification`
- Backend: Spring Boot on `127.0.0.1:8080`
- Oracle container: `ai-ticket-oracle`, port `1521`
- Redis container: `ai-ticket-redis`, port `6379`
- Embedding provider: SiliconFlow `Qwen/Qwen3-Embedding-8B`
- Chat provider: local OpenAI-compatible mock on `127.0.0.1:18080`
- Secret handling: `AI_EMBEDDING_API_KEY` was loaded from `/private/tmp/ai-ticket-secrets/siliconflow.env`; keys and JWTs were not printed

The real live chat provider call was skipped because `AI_CHAT_API_KEY` was not available locally. The live run still exercised the production chat adapter shape through the local OpenAI-compatible mock while retrieval used real SiliconFlow embeddings and Oracle vector search.

## Unit Verification

Focused tests after live-hardening changes:

```text
RagChatControllerTest, OpenAiCompatibleChatClientTest, RagChatServiceTest, AiChatMapperXmlTest
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Full backend suite:

```text
mvn -Dmaven.repo.local=/Users/xianghuaifeng/Documents/毕业设计/.worktrees/knowledge-live-verification/.m2repo test
Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Live Verification

Flyway startup confirmed the Phase 4 schema:

```text
Successfully validated 4 migrations
Current version of schema "AI_TICKET": 4
Schema "AI_TICKET" is up to date. No migration necessary.
```

Sanitized API verification output:

```json
[
  {
    "step": "admin_login",
    "status": 200,
    "tokenPresent": true,
    "chatAskPermission": false,
    "kbUploadPermission": true
  },
  {
    "step": "user_login",
    "status": 200,
    "tokenPresent": true,
    "chatAskPermission": true,
    "historyPermission": true
  },
  {
    "step": "create_kb_text",
    "status": 200,
    "documentId": 10,
    "parseStatus": "PARSE_SUCCESS"
  },
  {
    "step": "ask",
    "status": 200,
    "sessionId": 7,
    "userMessageId": 13,
    "assistantMessageId": 14,
    "answerPresent": true,
    "canAnswer": true,
    "confidence": 0.66,
    "citationCount": 5,
    "rawVectorFieldsPresent": false
  },
  {
    "step": "sessions",
    "status": 200,
    "count": 7
  },
  {
    "step": "messages",
    "status": 200,
    "count": 2,
    "assistantCitationCount": 5,
    "rawVectorFieldsPresent": false
  },
  {
    "step": "sse",
    "status": 200,
    "events": [
      "token",
      "token",
      "metadata"
    ],
    "hasToken": true,
    "hasMetadata": true,
    "textLength": 1519
  },
  {
    "step": "admin_ask_forbidden",
    "status": 403
  },
  {
    "step": "anonymous_history_unauthorized",
    "status": 401
  }
]
```

## Hardening Notes

The live run exposed three backend hardening fixes:

- Oracle/MyBatis boolean-like fields are now persisted as explicit numeric flags to avoid driver binding ambiguity.
- SSE catch paths now complete the emitter after sending a sanitized error event instead of surfacing a post-response exception to the container.
- Spring Security permits `ASYNC` and `ERROR` dispatcher types so completed SSE responses are not re-filtered and rejected after the response has already been committed.

## Result

Phase 4 backend RAG chat is live-verified for HTTP ask, deterministic SSE streaming, message/session history, citation persistence, RBAC, and no raw vector exposure. The provider integration remains extensible through the `ChatClient` abstraction and OpenAI-compatible adapter; swapping from mock chat to a live provider requires only valid `AI_CHAT_*` configuration.
