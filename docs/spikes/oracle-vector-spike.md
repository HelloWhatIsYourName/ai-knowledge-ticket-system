# Oracle 23ai Vector Spike Report

## Environment

- Oracle image: `gvenzl/oracle-free:23-slim`
- Vector dimension: `1024`
- Embedding provider: Aliyun Bailian `text-embedding-v3`
- Query function: `VECTOR_DISTANCE(embedding, TO_VECTOR(:queryVector), COSINE)`

## Result

- [ ] Docker Oracle started.
- [ ] Flyway created `vector_spike`.
- [ ] Spring Boot inserted `VECTOR(1024, FLOAT32)` records.
- [ ] Spring Boot queried Top-K by cosine distance.
- [ ] MyBatis vector parameter binding approach confirmed.
- [ ] Aliyun Bailian single embedding call returned 1024 dimensions.
- [ ] Aliyun Bailian batch embedding call returned 1024 dimensions for each input.

## Decision

Use Oracle 23ai `VECTOR(1024, FLOAT32)` with Aliyun Bailian `text-embedding-v3` for first-version RAG.

## Notes

During execution, append the actual SQL, request samples, command outputs, errors encountered, and final workaround under this section before checking this task complete.
