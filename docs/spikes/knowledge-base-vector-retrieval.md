# Knowledge Base Vector Retrieval Spike

Date: 2026-06-20

## Scope

This verification covers the Phase 3 knowledge-base backend foundation:

- plain-text document creation through the secured REST API
- synchronous SiliconFlow embedding ingestion
- Oracle 23ai `VECTOR(1024, FLOAT32)` chunk persistence and Top-K retrieval
- RBAC denial for a regular user without upload permission

RAG answer generation, SSE streaming, chat persistence, and ticket auto-transfer remain out of scope for this phase.

## Environment

- Branch/worktree: `knowledge-live-verification`
- Backend: Spring Boot on `127.0.0.1:8080`
- Oracle container: `ai-ticket-oracle`, healthy, port `1521`
- Redis container: `ai-ticket-redis`, healthy, port `6379`
- Embedding provider: SiliconFlow `Qwen/Qwen3-Embedding-8B`
- Secret handling: `AI_EMBEDDING_API_KEY` loaded from local environment; keys and JWTs were not printed

## Live Verification

Sanitized API verification output:

```json
[
  {
    "step": "admin_login",
    "status": 200,
    "tokenPresent": true
  },
  {
    "step": "admin_create_text_document",
    "status": 200,
    "id": 6,
    "title": "密码重置操作指南",
    "parseStatus": "PARSE_SUCCESS",
    "enabled": true
  },
  {
    "step": "chunk_list",
    "status": 200,
    "chunkCount": 1,
    "firstChunkPreview": "当用户忘记密码时，可以在登录页点击忘记密码，输入绑定手机号并完成验证码校验。校验"
  },
  {
    "step": "knowledge_search",
    "status": 200,
    "resultCount": 1,
    "top": {
      "sourceTitle": "密码重置操作指南",
      "similarity": 0.6568153699852133,
      "hasEmbeddingField": false
    }
  },
  {
    "step": "ordinary_user_upload_denial",
    "loginStatus": 200,
    "uploadStatus": 403
  },
  {
    "step": "overall",
    "ok": true
  }
]
```

## Debugging Note

The first live ingestion attempt exposed Oracle error `ORA-17004: Invalid column type: 1111` when MyBatis bound nullable parameters without explicit JDBC types. The fix declares JDBC types for nullable mapper parameters:

- `parseError`: `jdbcType=VARCHAR`
- `chunk.sourcePage`: `jdbcType=NUMERIC`

The document controller now also rethrows synchronous ingestion failures when the document remains `PENDING_PARSE`, preventing a false success response if ingestion fails before status transition.

## Result

Phase 3 retrieval primitives are live-verified: an admin can create a knowledge document, ingestion stores a chunk with a real embedding, search returns the relevant source without exposing raw vector fields, and regular users are denied upload access.
