# Oracle 23ai Vector Spike Report

## Environment

- Oracle image: `gvenzl/oracle-free:23-slim`
- Vector dimension: `1024`
- Embedding provider: SiliconFlow `Qwen/Qwen3-Embedding-8B`
- Query function: `VECTOR_DISTANCE(embedding, TO_VECTOR(:queryVector), COSINE)`

## Result

- [x] Docker Oracle started.
- [x] Flyway created `vector_spike`.
- [x] Spring Boot inserted `VECTOR(1024, FLOAT32)` records.
- [x] Spring Boot queried Top-K by cosine distance.
- [x] MyBatis vector parameter binding approach confirmed.
- [x] SiliconFlow single embedding call returned 1024 dimensions. ⭐
- [x] SiliconFlow batch embedding call returned 1024 dimensions for each input. ⭐

## Decision

Use Oracle 23ai `VECTOR(1024, FLOAT32)` with SiliconFlow `Qwen/Qwen3-Embedding-8B` for first-version RAG.

## Notes

### 2026-06-19 Verification

Docker services:

```text
NAME               IMAGE                        STATUS                    PORTS
ai-ticket-oracle   gvenzl/oracle-free:23-slim   Up 33 seconds (healthy)   0.0.0.0:1521->1521/tcp
ai-ticket-redis    redis:7-alpine               Up 33 seconds (healthy)   0.0.0.0:6379->6379/tcp
```

Spring Boot was started with Java 21:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21 mvn spring-boot:run
```

Flyway connected to Oracle 23ai and applied the vector spike migration:

```text
Database: jdbc:oracle:thin:@localhost:1521/FREEPDB1 (Oracle 23.26)
Successfully validated 1 migration
Migrating schema "AI_TICKET" to version "1 - vector spike"
Successfully applied 1 migration to schema "AI_TICKET", now at version v1
```

Manual insert request used a generated 1024-dimensional float vector:

```bash
curl -sS -X POST http://localhost:8080/api/spike/vector \
  -H 'Content-Type: application/json' \
  -d @/private/tmp/vector-insert.json
```

Response:

```json
{"success":true,"data":null,"message":"ok"}
```

Manual Top-K search request used a nearby generated 1024-dimensional query vector:

```bash
curl -sS -X POST http://localhost:8080/api/spike/vector/search \
  -H 'Content-Type: application/json' \
  -d @/private/tmp/vector-search.json
```

Response:

```json
{"success":true,"data":[{"id":1,"content":"oracle vector spike sample alpha","distance":9.37601885020456E-10}],"message":"ok"}
```

The MyBatis XML binding approach is:

```xml
VALUES (#{content}, TO_VECTOR(#{vectorLiteral}))
VECTOR_DISTANCE(embedding, TO_VECTOR(#{queryVectorLiteral}), COSINE)
```

The test run with Java 21 passed:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

SiliconFlow live embedding calls were initially deferred because `AI_EMBEDDING_API_KEY` was not present in the local environment. The adapter contract and `EmbeddingResult` dimensional validation were covered by local tests before the live key was configured.

### 2026-06-19 SiliconFlow Live Verification

SiliconFlow `Qwen/Qwen3-Embedding-8B` live single and batch calls returned 1024-dimensional vectors through the Spring Boot `EmbeddingClient`.

```text
single embedding dimensions=1024
batch embedding count=2 dimensions=1024,1024
```

A real SiliconFlow provider vector was written to Oracle `VECTOR(1024, FLOAT32)` through `POST /api/spike/vector`:

```json
{"success":true,"data":null,"message":"ok"}
```

The same real provider vector was used as a query through `POST /api/spike/vector/search`, and the inserted verification row was returned as the first Top-K result:

```json
{"id":22,"content":"siliconflow-live-provider-vector","distance":2.220446049250313E-16}
```

No API key or full embedding vector was written to this report.
