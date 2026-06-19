# Knowledge Base Vector Retrieval Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Progress rule for this project:** after a full task is implemented, verified, reviewed, and committed, append `⭐` to that task heading line and keep the step checkboxes accurate. Example: `### Task 2: Text Chunking Service ⭐`.

**Goal:** Build the Phase 3 backend foundation for knowledge-base documents, deterministic text chunking, SiliconFlow embedding ingestion, Oracle 23ai vector storage, and protected Top-K retrieval.

**Architecture:** This phase adds a focused Knowledge Base module that owns document metadata, chunk persistence, parse status transitions, embedding ingestion, and vector retrieval. It reuses the existing `EmbeddingClient`, `EmbeddingResult`, `OracleVectorLiteral`, Spring Security RBAC permissions, Flyway, and MyBatis XML vector patterns proven in earlier phases. Full AI RAG answer generation, SSE streaming, chat history, and auto ticket creation stay in the next phase; this phase exposes retrieval-ready backend primitives.

**Tech Stack:** Java 21, Spring Boot 3, Spring Security method authorization, MyBatis XML, Flyway, Oracle Database 23ai `VECTOR(1024, FLOAT32)`, Redis Stream foundation, SiliconFlow `Qwen/Qwen3-Embedding-8B`, JUnit 5.

---

## Scope

This plan implements:

1. `kb_category`, `kb_document`, and `kb_chunk` schema with parse status checks and Oracle vector storage.
2. Knowledge configuration for chunk size, overlap, top-k, minimum similarity, retry limit, and Redis Stream names.
3. Deterministic paragraph-first text chunking with length fallback and overlap.
4. MyBatis mapper layer for document metadata, chunk replacement, parse status transitions, and Top-K vector search.
5. Embedding ingestion service that chunks plain text, calls `EmbeddingClient.embedBatch`, validates 1024 dimensions through `EmbeddingResult`, and persists chunks via `TO_VECTOR(...)`.
6. Retrieval service that embeds a query and searches enabled, parse-success documents only.
7. Minimal RBAC-protected REST endpoints for text-document ingestion, document listing/detail, enable/disable, retry-state reset, chunk listing, and retrieval search.
8. Redis Stream enqueue adapter for future async parsing while keeping a synchronous ingestion endpoint for reliable Phase 3 verification.
9. Unit tests and live verification steps against local Oracle, Redis, JWT auth, and SiliconFlow when `AI_EMBEDDING_API_KEY` is present in the environment.

This plan intentionally does not implement:

1. Multipart upload and Apache Tika parsing of PDF/Word files.
2. Full AI answer generation and prompt templates.
3. SSE streaming.
4. AI session/message/citation persistence.
5. Auto transfer to tickets.
6. Frontend pages.

Those are Phase 4+ responsibilities. The schema and service boundaries below keep extension points for them.

## Subagent Strategy

Use subagents after this plan is saved. The main agent remains responsible for architecture, file-boundary decisions, integration, review, and final verification.

Good cheaper-model subagent tasks:

1. Task 1 SQL migration drafting and syntax review.
2. Task 2 chunking tests and implementation.
3. Task 5 request/response DTO boilerplate.
4. Task 7 documentation updates and command transcript cleanup.

Use the main agent or a stronger model for:

1. Task 3 mapper XML with Oracle vector SQL.
2. Task 4 embedding ingestion service integration.
3. Task 6 controller/security integration.
4. Debugging live Oracle, Redis, or SiliconFlow failures.
5. Final code review and plan progress marking.

Do not dispatch multiple implementation subagents in parallel when they edit the same files. Each task must be reviewed before the next task begins.

## File Structure

```text
backend/
  pom.xml
  src/main/java/com/example/aiticket/config/KnowledgeProperties.java
  src/main/java/com/example/aiticket/knowledge/config/KnowledgeModuleConfig.java
  src/main/java/com/example/aiticket/knowledge/domain/KnowledgeDocument.java
  src/main/java/com/example/aiticket/knowledge/domain/KnowledgeChunk.java
  src/main/java/com/example/aiticket/knowledge/domain/KnowledgeCategory.java
  src/main/java/com/example/aiticket/knowledge/domain/KnowledgeParseStatus.java
  src/main/java/com/example/aiticket/knowledge/domain/KnowledgeChunkDraft.java
  src/main/java/com/example/aiticket/knowledge/domain/KnowledgeSearchResult.java
  src/main/java/com/example/aiticket/knowledge/domain/TextChunk.java
  src/main/java/com/example/aiticket/knowledge/chunk/TextChunker.java
  src/main/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunker.java
  src/main/java/com/example/aiticket/knowledge/mapper/KnowledgeDocumentMapper.java
  src/main/java/com/example/aiticket/knowledge/mapper/KnowledgeChunkMapper.java
  src/main/java/com/example/aiticket/knowledge/service/KnowledgeDocumentService.java
  src/main/java/com/example/aiticket/knowledge/service/KnowledgeIngestionService.java
  src/main/java/com/example/aiticket/knowledge/service/KnowledgeRetrievalService.java
  src/main/java/com/example/aiticket/knowledge/queue/KnowledgeParseQueue.java
  src/main/java/com/example/aiticket/knowledge/queue/RedisKnowledgeParseQueue.java
  src/main/java/com/example/aiticket/knowledge/web/KnowledgeDocumentController.java
  src/main/java/com/example/aiticket/knowledge/web/KnowledgeSearchController.java
  src/main/java/com/example/aiticket/knowledge/web/CreateTextDocumentRequest.java
  src/main/java/com/example/aiticket/knowledge/web/DocumentResponse.java
  src/main/java/com/example/aiticket/knowledge/web/ChunkResponse.java
  src/main/java/com/example/aiticket/knowledge/web/SearchKnowledgeRequest.java
  src/main/java/com/example/aiticket/knowledge/web/SearchKnowledgeResponse.java
  src/main/resources/db/migration/V3__knowledge_base.sql
  src/main/resources/mapper/KnowledgeDocumentMapper.xml
  src/main/resources/mapper/KnowledgeChunkMapper.xml
  src/test/java/com/example/aiticket/config/KnowledgePropertiesTest.java
  src/test/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunkerTest.java
  src/test/java/com/example/aiticket/knowledge/service/KnowledgeIngestionServiceTest.java
  src/test/java/com/example/aiticket/knowledge/service/KnowledgeRetrievalServiceTest.java
docs/
  spikes/knowledge-base-vector-retrieval.md
  superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
```

## Data and API Decisions

Use these constants across tasks:

```text
Default max chunk chars: 700
Default overlap chars: 100
Default topK: 5
Default minSimilarity: 0.70
Default max retry count: 3
Redis Stream: stream:kb:parse
Redis Consumer Group: kb-parser-group
Embedding dimensions: ai.embedding.dimensions, currently 1024
```

Use these status values:

```text
PENDING_PARSE
PARSING
PARSE_SUCCESS
PARSE_FAILED
```

Use these endpoints:

```text
POST /api/kb/documents/text
GET  /api/kb/documents
GET  /api/kb/documents/{id}
POST /api/kb/documents/{id}/enable
POST /api/kb/documents/{id}/disable
POST /api/kb/documents/{id}/retry-parse
GET  /api/kb/documents/{id}/chunks
POST /api/kb/search
```

Authorization:

```text
knowledge:document:upload -> POST /api/kb/documents/text
knowledge:document:manage -> enable, disable, retry-parse
knowledge:document:view   -> list, detail, chunks, search
```

For Phase 3, `POST /api/kb/documents/text` ingests plain text synchronously after creating metadata. It may also enqueue a Redis Stream message for the future async consumer, but the synchronous path is the verified path for this phase.

## Task 1: Knowledge Configuration and Schema ⭐

**Files:**
- Create: `backend/src/main/java/com/example/aiticket/config/KnowledgeProperties.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/config/KnowledgeModuleConfig.java`
- Modify: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/db/migration/V3__knowledge_base.sql`
- Test: `backend/src/test/java/com/example/aiticket/config/KnowledgePropertiesTest.java`

- [x] **Step 1: Write failing configuration test**

Create `backend/src/test/java/com/example/aiticket/config/KnowledgePropertiesTest.java`:

```java
package com.example.aiticket.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgePropertiesTest {
    @Test
    void bindsKnowledgeDefaultsAndOverrides() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "knowledge.chunk.max-chars", "640",
                "knowledge.chunk.overlap-chars", "96",
                "knowledge.retrieval.top-k", "6",
                "knowledge.retrieval.min-similarity", "0.72",
                "knowledge.parse.max-retry-count", "3",
                "knowledge.queue.stream-key", "stream:kb:parse",
                "knowledge.queue.consumer-group", "kb-parser-group"
        ));

        BindResult<KnowledgeProperties> result = new Binder(source)
                .bind("knowledge", KnowledgeProperties.class);

        assertThat(result.isBound()).isTrue();
        KnowledgeProperties properties = result.get();
        assertThat(properties.getChunk().getMaxChars()).isEqualTo(640);
        assertThat(properties.getChunk().getOverlapChars()).isEqualTo(96);
        assertThat(properties.getRetrieval().getTopK()).isEqualTo(6);
        assertThat(properties.getRetrieval().getMinSimilarity()).isEqualTo(0.72);
        assertThat(properties.getParse().getMaxRetryCount()).isEqualTo(3);
        assertThat(properties.getQueue().getStreamKey()).isEqualTo("stream:kb:parse");
        assertThat(properties.getQueue().getConsumerGroup()).isEqualTo("kb-parser-group");
    }

    @Test
    void providesSafeDefaults() {
        KnowledgeProperties properties = new KnowledgeProperties();

        assertThat(properties.getChunk().getMaxChars()).isEqualTo(700);
        assertThat(properties.getChunk().getOverlapChars()).isEqualTo(100);
        assertThat(properties.getRetrieval().getTopK()).isEqualTo(5);
        assertThat(properties.getRetrieval().getMinSimilarity()).isEqualTo(0.70);
        assertThat(properties.getParse().getMaxRetryCount()).isEqualTo(3);
        assertThat(properties.getQueue().getStreamKey()).isEqualTo("stream:kb:parse");
        assertThat(properties.getQueue().getConsumerGroup()).isEqualTo("kb-parser-group");
    }

    @Test
    void rejectsUnsafeChunkAndRetrievalSettings() {
        KnowledgeProperties properties = new KnowledgeProperties();
        properties.getChunk().setMaxChars(700);
        properties.getChunk().setOverlapChars(700);
        properties.getRetrieval().setTopK(100);
        properties.getParse().setMaxRetryCount(100);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "chunk.overlapSmallerThanMaxChars",
                        "retrieval.topK",
                        "parse.maxRetryCount"
                );
    }
}
```

- [x] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
mvn test -Dtest=KnowledgePropertiesTest
```

Expected: compilation fails because `KnowledgeProperties` does not exist.

- [x] **Step 3: Add `KnowledgeProperties`**

Create `backend/src/main/java/com/example/aiticket/config/KnowledgeProperties.java`:

```java
package com.example.aiticket.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "knowledge")
public class KnowledgeProperties {
    @Valid
    private Chunk chunk = new Chunk();

    @Valid
    private Retrieval retrieval = new Retrieval();

    @Valid
    private Parse parse = new Parse();

    @Valid
    private Queue queue = new Queue();

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public Retrieval getRetrieval() {
        return retrieval;
    }

    public void setRetrieval(Retrieval retrieval) {
        this.retrieval = retrieval;
    }

    public Parse getParse() {
        return parse;
    }

    public void setParse(Parse parse) {
        this.parse = parse;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public static class Chunk {
        @Min(100)
        private int maxChars = 700;

        @Min(0)
        private int overlapChars = 100;

        public int getMaxChars() {
            return maxChars;
        }

        public void setMaxChars(int maxChars) {
            this.maxChars = maxChars;
        }

        public int getOverlapChars() {
            return overlapChars;
        }

        public void setOverlapChars(int overlapChars) {
            this.overlapChars = overlapChars;
        }

        @AssertTrue(message = "overlapChars must be smaller than maxChars")
        public boolean isOverlapSmallerThanMaxChars() {
            return overlapChars < maxChars;
        }
    }

    public static class Retrieval {
        @Min(1)
        @Max(20)
        private int topK = 5;

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private double minSimilarity = 0.70;

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public double getMinSimilarity() {
            return minSimilarity;
        }

        public void setMinSimilarity(double minSimilarity) {
            this.minSimilarity = minSimilarity;
        }
    }

    public static class Parse {
        @Min(0)
        @Max(10)
        private int maxRetryCount = 3;

        public int getMaxRetryCount() {
            return maxRetryCount;
        }

        public void setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }
    }

    public static class Queue {
        @NotBlank
        private String streamKey = "stream:kb:parse";

        @NotBlank
        private String consumerGroup = "kb-parser-group";

        public String getStreamKey() {
            return streamKey;
        }

        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }
    }
}
```

- [x] **Step 4: Register configuration properties**

Create `backend/src/main/java/com/example/aiticket/knowledge/config/KnowledgeModuleConfig.java`:

```java
package com.example.aiticket.knowledge.config;

import com.example.aiticket.config.KnowledgeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KnowledgeProperties.class)
public class KnowledgeModuleConfig {
}
```

- [x] **Step 5: Add `knowledge` defaults to `application.yml`**

Append this section to `backend/src/main/resources/application.yml`:

```yaml
knowledge:
  chunk:
    max-chars: ${KNOWLEDGE_CHUNK_MAX_CHARS:700}
    overlap-chars: ${KNOWLEDGE_CHUNK_OVERLAP_CHARS:100}
  retrieval:
    top-k: ${KNOWLEDGE_RETRIEVAL_TOP_K:5}
    min-similarity: ${KNOWLEDGE_RETRIEVAL_MIN_SIMILARITY:0.70}
  parse:
    max-retry-count: ${KNOWLEDGE_PARSE_MAX_RETRY_COUNT:3}
  queue:
    stream-key: ${KNOWLEDGE_QUEUE_STREAM_KEY:stream:kb:parse}
    consumer-group: ${KNOWLEDGE_QUEUE_CONSUMER_GROUP:kb-parser-group}
```

- [x] **Step 6: Add Flyway schema migration**

Create `backend/src/main/resources/db/migration/V3__knowledge_base.sql`:

```sql
CREATE TABLE kb_category (
    id NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    parent_id NUMBER(19),
    sort_order NUMBER(10) DEFAULT 0 NOT NULL,
    enabled NUMBER(1) DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_kb_category_name UNIQUE (name),
    CONSTRAINT ck_kb_category_enabled CHECK (enabled IN (0, 1)),
    CONSTRAINT fk_kb_category_parent FOREIGN KEY (parent_id) REFERENCES kb_category(id)
);

CREATE TABLE kb_document (
    id NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    title VARCHAR2(200) NOT NULL,
    category_id NUMBER(19),
    file_name VARCHAR2(255),
    storage_name VARCHAR2(255),
    file_type VARCHAR2(32) DEFAULT 'TEXT' NOT NULL,
    file_size NUMBER(19) DEFAULT 0 NOT NULL,
    enabled NUMBER(1) DEFAULT 1 NOT NULL,
    parse_status VARCHAR2(32) DEFAULT 'PENDING_PARSE' NOT NULL,
    parse_error VARCHAR2(1000),
    retry_count NUMBER(10) DEFAULT 0 NOT NULL,
    uploaded_by NUMBER(19),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT fk_kb_document_category FOREIGN KEY (category_id) REFERENCES kb_category(id),
    CONSTRAINT fk_kb_document_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES sys_user(id),
    CONSTRAINT ck_kb_document_enabled CHECK (enabled IN (0, 1)),
    CONSTRAINT ck_kb_document_deleted CHECK (deleted IN (0, 1)),
    CONSTRAINT ck_kb_document_parse_status CHECK (parse_status IN ('PENDING_PARSE', 'PARSING', 'PARSE_SUCCESS', 'PARSE_FAILED'))
);

CREATE TABLE kb_chunk (
    id NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    document_id NUMBER(19) NOT NULL,
    category_id NUMBER(19),
    chunk_index NUMBER(10) NOT NULL,
    content CLOB NOT NULL,
    content_hash VARCHAR2(64) NOT NULL,
    source_title VARCHAR2(200) NOT NULL,
    source_page NUMBER(10),
    embedding VECTOR(1024, FLOAT32) NOT NULL,
    enabled NUMBER(1) DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_kb_chunk_document FOREIGN KEY (document_id) REFERENCES kb_document(id),
    CONSTRAINT fk_kb_chunk_category FOREIGN KEY (category_id) REFERENCES kb_category(id),
    CONSTRAINT ck_kb_chunk_enabled CHECK (enabled IN (0, 1)),
    CONSTRAINT uk_kb_chunk_document_index UNIQUE (document_id, chunk_index)
);

CREATE INDEX idx_kb_document_status ON kb_document (parse_status, enabled, deleted);
CREATE INDEX idx_kb_document_category ON kb_document (category_id);
CREATE INDEX idx_kb_chunk_document ON kb_chunk (document_id);
CREATE INDEX idx_kb_chunk_category ON kb_chunk (category_id);
CREATE INDEX idx_kb_chunk_enabled ON kb_chunk (enabled);
CREATE INDEX idx_kb_chunk_hash ON kb_chunk (content_hash);

INSERT INTO kb_category (id, name, sort_order, enabled) VALUES (1, '默认分类', 1, 1);
```

- [x] **Step 7: Run focused tests**

Run:

```bash
cd backend
mvn test -Dtest=KnowledgePropertiesTest
```

Expected: test passes.

- [x] **Step 8: Commit Task 1**

Run:

```bash
git add backend/src/main/java/com/example/aiticket/config/KnowledgeProperties.java backend/src/main/java/com/example/aiticket/knowledge/config/KnowledgeModuleConfig.java backend/src/main/resources/application.yml backend/src/main/resources/db/migration/V3__knowledge_base.sql backend/src/test/java/com/example/aiticket/config/KnowledgePropertiesTest.java docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
git commit -m "feat: add knowledge base schema"
```

After commit and review, append `⭐` to the Task 1 heading.

## Task 2: Deterministic Text Chunking ⭐

**Files:**
- Create: `backend/src/main/java/com/example/aiticket/knowledge/domain/TextChunk.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/chunk/TextChunker.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunker.java`
- Test: `backend/src/test/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunkerTest.java`

- [x] **Step 1: Write failing chunker tests**

Create `backend/src/test/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunkerTest.java`:

```java
package com.example.aiticket.knowledge.chunk;

import com.example.aiticket.knowledge.domain.TextChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParagraphTextChunkerTest {
    @Test
    void splitsParagraphsAndPreservesIndexes() {
        ParagraphTextChunker chunker = new ParagraphTextChunker(80, 10);

        List<TextChunk> chunks = chunker.chunk("""
                第一段介绍知识库系统，用于验证自然段优先切分。

                第二段说明上传文档以后会进行解析、切片、向量化和检索。
                """);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).chunkIndex()).isEqualTo(0);
        assertThat(chunks.get(1).chunkIndex()).isEqualTo(1);
        assertThat(chunks.get(0).content()).contains("第一段介绍知识库系统");
        assertThat(chunks.get(1).content()).contains("第二段说明上传文档");
    }

    @Test
    void splitsLongParagraphWithOverlap() {
        ParagraphTextChunker chunker = new ParagraphTextChunker(20, 5);

        List<TextChunk> chunks = chunker.chunk("012345678901234567890123456789");

        assertThat(chunks).extracting(TextChunk::content)
                .containsExactly("01234567890123456789", "567890123456789");
    }

    @Test
    void removesBlankChunks() {
        ParagraphTextChunker chunker = new ParagraphTextChunker(20, 5);

        assertThat(chunker.chunk(" \n\n\t ")).isEmpty();
    }

    @Test
    void preservesFormattingInsideNonBlankChunks() {
        ParagraphTextChunker chunker = new ParagraphTextChunker(80, 10);

        List<TextChunk> chunks = chunker.chunk("  - 第一步\n    保留缩进  ");

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().content()).isEqualTo("  - 第一步\n    保留缩进  ");
    }

    @Test
    void rejectsOverlapGreaterThanOrEqualToMaxChars() {
        assertThatThrownBy(() -> new ParagraphTextChunker(100, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlapChars must be smaller");
    }
}
```

- [x] **Step 2: Run tests and verify they fail**

Run:

```bash
cd backend
mvn test -Dtest=ParagraphTextChunkerTest
```

Expected: compilation fails because chunker classes do not exist.

- [x] **Step 3: Create `TextChunk` record**

Create `backend/src/main/java/com/example/aiticket/knowledge/domain/TextChunk.java`:

```java
package com.example.aiticket.knowledge.domain;

public record TextChunk(int chunkIndex, String content) {
    public TextChunk {
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex must be non-negative");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }
}
```

- [x] **Step 4: Create chunker interface**

Create `backend/src/main/java/com/example/aiticket/knowledge/chunk/TextChunker.java`:

```java
package com.example.aiticket.knowledge.chunk;

import com.example.aiticket.knowledge.domain.TextChunk;

import java.util.List;

public interface TextChunker {
    List<TextChunk> chunk(String text);
}
```

- [x] **Step 5: Implement paragraph-first chunker**

Create `backend/src/main/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunker.java`:

```java
package com.example.aiticket.knowledge.chunk;

import com.example.aiticket.knowledge.domain.TextChunk;

import java.util.ArrayList;
import java.util.List;

public class ParagraphTextChunker implements TextChunker {
    private final int maxChars;
    private final int overlapChars;

    public ParagraphTextChunker(int maxChars, int overlapChars) {
        if (maxChars < 1) {
            throw new IllegalArgumentException("maxChars must be positive");
        }
        if (overlapChars < 0) {
            throw new IllegalArgumentException("overlapChars must be non-negative");
        }
        if (overlapChars >= maxChars) {
            throw new IllegalArgumentException("overlapChars must be smaller than maxChars");
        }
        this.maxChars = maxChars;
        this.overlapChars = overlapChars;
    }

    @Override
    public List<TextChunk> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> parts = new ArrayList<>();
        for (String paragraph : text.replace("\r\n", "\n").split("\\n\\s*\\n")) {
            if (paragraph.isBlank()) {
                continue;
            }
            if (paragraph.length() <= maxChars) {
                parts.add(paragraph);
            } else {
                parts.addAll(splitLongParagraph(paragraph));
            }
        }

        List<TextChunk> chunks = new ArrayList<>(parts.size());
        for (int i = 0; i < parts.size(); i++) {
            chunks.add(new TextChunk(i, parts.get(i)));
        }
        return chunks;
    }

    private List<String> splitLongParagraph(String paragraph) {
        List<String> parts = new ArrayList<>();
        int step = maxChars - overlapChars;
        int start = 0;
        while (start < paragraph.length()) {
            int end = Math.min(start + maxChars, paragraph.length());
            String chunk = paragraph.substring(start, end);
            if (!chunk.isBlank()) {
                parts.add(chunk);
            }
            if (end == paragraph.length()) {
                break;
            }
            start += step;
        }
        return parts;
    }
}
```

- [x] **Step 6: Register chunker bean**

Modify `backend/src/main/java/com/example/aiticket/knowledge/config/KnowledgeModuleConfig.java`:

```java
package com.example.aiticket.knowledge.config;

import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.chunk.ParagraphTextChunker;
import com.example.aiticket.knowledge.chunk.TextChunker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KnowledgeProperties.class)
public class KnowledgeModuleConfig {
    @Bean
    TextChunker textChunker(KnowledgeProperties properties) {
        return new ParagraphTextChunker(
                properties.getChunk().getMaxChars(),
                properties.getChunk().getOverlapChars()
        );
    }
}
```

- [x] **Step 7: Run focused tests**

Run:

```bash
cd backend
mvn test -Dtest=ParagraphTextChunkerTest,KnowledgePropertiesTest
```

Expected: both test classes pass.

- [x] **Step 8: Commit Task 2**

Run:

```bash
git add backend/src/main/java/com/example/aiticket/knowledge/domain/TextChunk.java backend/src/main/java/com/example/aiticket/knowledge/chunk/TextChunker.java backend/src/main/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunker.java backend/src/main/java/com/example/aiticket/knowledge/config/KnowledgeModuleConfig.java backend/src/test/java/com/example/aiticket/knowledge/chunk/ParagraphTextChunkerTest.java docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
git commit -m "feat: add knowledge text chunking"
```

After commit and review, append `⭐` to the Task 2 heading.

## Task 3: Knowledge Mapper Layer and Vector SQL

**Files:**
- Create domain records under `backend/src/main/java/com/example/aiticket/knowledge/domain/`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/mapper/KnowledgeDocumentMapper.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/mapper/KnowledgeChunkMapper.java`
- Create: `backend/src/main/resources/mapper/KnowledgeDocumentMapper.xml`
- Create: `backend/src/main/resources/mapper/KnowledgeChunkMapper.xml`

- [x] **Step 1: Create domain records**

Create these files:

```java
// backend/src/main/java/com/example/aiticket/knowledge/domain/KnowledgeParseStatus.java
package com.example.aiticket.knowledge.domain;

public enum KnowledgeParseStatus {
    PENDING_PARSE,
    PARSING,
    PARSE_SUCCESS,
    PARSE_FAILED
}
```

```java
// backend/src/main/java/com/example/aiticket/knowledge/domain/KnowledgeDocument.java
package com.example.aiticket.knowledge.domain;

import java.time.LocalDateTime;

public record KnowledgeDocument(
        Long id,
        String title,
        Long categoryId,
        String fileName,
        String storageName,
        String fileType,
        Long fileSize,
        Boolean enabled,
        KnowledgeParseStatus parseStatus,
        String parseError,
        Integer retryCount,
        Long uploadedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
```

```java
// backend/src/main/java/com/example/aiticket/knowledge/domain/KnowledgeChunk.java
package com.example.aiticket.knowledge.domain;

import java.time.LocalDateTime;

public record KnowledgeChunk(
        Long id,
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String contentHash,
        String sourceTitle,
        Integer sourcePage,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
```

```java
// backend/src/main/java/com/example/aiticket/knowledge/domain/KnowledgeCategory.java
package com.example.aiticket.knowledge.domain;

public record KnowledgeCategory(Long id, String name, Long parentId, Integer sortOrder, Boolean enabled) {
}
```

```java
// backend/src/main/java/com/example/aiticket/knowledge/domain/KnowledgeChunkDraft.java
package com.example.aiticket.knowledge.domain;

public record KnowledgeChunkDraft(
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String contentHash,
        String sourceTitle,
        Integer sourcePage,
        String vectorLiteral
) {
}
```

```java
// backend/src/main/java/com/example/aiticket/knowledge/domain/KnowledgeSearchResult.java
package com.example.aiticket.knowledge.domain;

public record KnowledgeSearchResult(
        Long chunkId,
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String sourceTitle,
        Double distance,
        Double similarity
) {
}
```

- [x] **Step 2: Create mapper interfaces**

Create `backend/src/main/java/com/example/aiticket/knowledge/mapper/KnowledgeDocumentMapper.java`:

```java
package com.example.aiticket.knowledge.mapper;

import com.example.aiticket.knowledge.domain.KnowledgeDocument;
import com.example.aiticket.knowledge.domain.KnowledgeParseStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper {
    int insertTextDocument(@Param("title") String title,
                           @Param("categoryId") Long categoryId,
                           @Param("fileSize") long fileSize,
                           @Param("uploadedBy") Long uploadedBy);

    KnowledgeDocument findLatestByTitleAndUploader(@Param("title") String title,
                                                   @Param("uploadedBy") Long uploadedBy);

    KnowledgeDocument findById(@Param("id") Long id);

    List<KnowledgeDocument> findRecent(@Param("limit") int limit);

    int updateEnabled(@Param("id") Long id, @Param("enabled") int enabled);

    int updateParseStatus(@Param("id") Long id,
                          @Param("parseStatus") KnowledgeParseStatus parseStatus,
                          @Param("parseError") String parseError);

    int markParseFailed(@Param("id") Long id,
                        @Param("parseError") String parseError,
                        @Param("maxRetryCount") int maxRetryCount);

    int resetForRetry(@Param("id") Long id);
}
```

Create `backend/src/main/java/com/example/aiticket/knowledge/mapper/KnowledgeChunkMapper.java`:

```java
package com.example.aiticket.knowledge.mapper;

import com.example.aiticket.knowledge.domain.KnowledgeChunk;
import com.example.aiticket.knowledge.domain.KnowledgeChunkDraft;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper {
    int deleteByDocumentId(@Param("documentId") Long documentId);

    default int insertBatch(List<KnowledgeChunkDraft> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return 0;
        }
        return insertBatchNonEmpty(chunks);
    }

    int insertBatchNonEmpty(@Param("chunks") List<KnowledgeChunkDraft> chunks);

    List<KnowledgeChunk> findByDocumentId(@Param("documentId") Long documentId);

    List<KnowledgeSearchResult> search(@Param("queryVectorLiteral") String queryVectorLiteral,
                                       @Param("categoryId") Long categoryId,
                                       @Param("minSimilarity") double minSimilarity,
                                       @Param("limit") int limit);
}
```

- [x] **Step 3: Create `KnowledgeDocumentMapper.xml`**

Create `backend/src/main/resources/mapper/KnowledgeDocumentMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.aiticket.knowledge.mapper.KnowledgeDocumentMapper">
    <insert id="insertTextDocument">
        INSERT INTO kb_document (
            title, category_id, file_name, storage_name, file_type, file_size,
            enabled, parse_status, retry_count, uploaded_by
        )
        VALUES (
            #{title}, #{categoryId}, #{title}, NULL, 'TEXT', #{fileSize},
            1, 'PENDING_PARSE', 0, #{uploadedBy}
        )
    </insert>

    <select id="findLatestByTitleAndUploader" resultType="com.example.aiticket.knowledge.domain.KnowledgeDocument">
        SELECT id, title, category_id, file_name, storage_name, file_type, file_size,
               enabled, parse_status, parse_error, retry_count, uploaded_by,
               created_at, updated_at, deleted
        FROM kb_document
        WHERE title = #{title}
          AND uploaded_by = #{uploadedBy}
          AND deleted = 0
        ORDER BY created_at DESC, id DESC
        FETCH FIRST 1 ROW ONLY
    </select>

    <select id="findById" resultType="com.example.aiticket.knowledge.domain.KnowledgeDocument">
        SELECT id, title, category_id, file_name, storage_name, file_type, file_size,
               enabled, parse_status, parse_error, retry_count, uploaded_by,
               created_at, updated_at, deleted
        FROM kb_document
        WHERE id = #{id}
          AND deleted = 0
    </select>

    <select id="findRecent" resultType="com.example.aiticket.knowledge.domain.KnowledgeDocument">
        SELECT id, title, category_id, file_name, storage_name, file_type, file_size,
               enabled, parse_status, parse_error, retry_count, uploaded_by,
               created_at, updated_at, deleted
        FROM kb_document
        WHERE deleted = 0
        ORDER BY created_at DESC
        FETCH FIRST #{limit} ROWS ONLY
    </select>

    <update id="updateEnabled">
        UPDATE kb_document
        SET enabled = #{enabled},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND deleted = 0
    </update>

    <update id="updateParseStatus">
        UPDATE kb_document
        SET parse_status = #{parseStatus},
            parse_error = #{parseError},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND deleted = 0
    </update>

    <update id="markParseFailed">
        UPDATE kb_document
        SET parse_status = CASE WHEN retry_count + 1 &gt;= #{maxRetryCount} THEN 'PARSE_FAILED' ELSE 'PENDING_PARSE' END,
            parse_error = #{parseError},
            retry_count = retry_count + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND deleted = 0
    </update>

    <update id="resetForRetry">
        UPDATE kb_document
        SET parse_status = 'PENDING_PARSE',
            parse_error = NULL,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND deleted = 0
          AND parse_status = 'PARSE_FAILED'
    </update>
</mapper>
```

- [x] **Step 4: Create `KnowledgeChunkMapper.xml`**

Create `backend/src/main/resources/mapper/KnowledgeChunkMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper">
    <delete id="deleteByDocumentId">
        DELETE FROM kb_chunk
        WHERE document_id = #{documentId}
    </delete>

    <insert id="insertBatchNonEmpty">
        INSERT ALL
        <foreach collection="chunks" item="chunk">
            INTO kb_chunk (
                document_id, category_id, chunk_index, content, content_hash,
                source_title, source_page, embedding, enabled
            )
            VALUES (
                #{chunk.documentId}, #{chunk.categoryId}, #{chunk.chunkIndex},
                #{chunk.content}, #{chunk.contentHash}, #{chunk.sourceTitle},
                #{chunk.sourcePage}, TO_VECTOR(#{chunk.vectorLiteral}), 1
            )
        </foreach>
        SELECT 1 FROM dual
    </insert>

    <select id="findByDocumentId" resultType="com.example.aiticket.knowledge.domain.KnowledgeChunk">
        SELECT id, document_id, category_id, chunk_index, content, content_hash,
               source_title, source_page, enabled, created_at, updated_at
        FROM kb_chunk
        WHERE document_id = #{documentId}
        ORDER BY chunk_index
    </select>

    <select id="search" resultType="com.example.aiticket.knowledge.domain.KnowledgeSearchResult">
        SELECT *
        FROM (
            SELECT c.id AS chunk_id,
                   c.document_id,
                   c.category_id,
                   c.chunk_index,
                   c.content,
                   c.source_title,
                   VECTOR_DISTANCE(c.embedding, TO_VECTOR(#{queryVectorLiteral}), COSINE) AS distance,
                   1 - VECTOR_DISTANCE(c.embedding, TO_VECTOR(#{queryVectorLiteral}), COSINE) AS similarity
            FROM kb_chunk c
            JOIN kb_document d ON d.id = c.document_id
            WHERE c.enabled = 1
              AND d.enabled = 1
              AND d.deleted = 0
              AND d.parse_status = 'PARSE_SUCCESS'
              <if test="categoryId != null">
                  AND c.category_id = #{categoryId}
              </if>
        )
        WHERE similarity &gt;= #{minSimilarity}
        ORDER BY distance
        FETCH FIRST #{limit} ROWS ONLY
    </select>
</mapper>
```

- [x] **Step 5: Run compile check**

Run:

```bash
cd backend
mvn test -DskipTests
```

Expected: compilation succeeds. If XML parsing fails, fix mapper syntax before continuing.

- [ ] **Step 6: Commit Task 3**

Run:

```bash
git add backend/src/main/java/com/example/aiticket/knowledge/domain backend/src/main/java/com/example/aiticket/knowledge/mapper backend/src/main/resources/mapper/KnowledgeDocumentMapper.xml backend/src/main/resources/mapper/KnowledgeChunkMapper.xml docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
git commit -m "feat: add knowledge mapper layer"
```

After commit and review, append `⭐` to the Task 3 heading.

## Task 4: Ingestion and Retrieval Services

**Files:**
- Create: `backend/src/main/java/com/example/aiticket/knowledge/service/KnowledgeDocumentService.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/service/KnowledgeIngestionService.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/service/KnowledgeRetrievalService.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/queue/KnowledgeParseQueue.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/queue/RedisKnowledgeParseQueue.java`
- Test: `backend/src/test/java/com/example/aiticket/knowledge/service/KnowledgeIngestionServiceTest.java`
- Test: `backend/src/test/java/com/example/aiticket/knowledge/service/KnowledgeRetrievalServiceTest.java`

- [ ] **Step 1: Write ingestion service test with fakes**

Create `backend/src/test/java/com/example/aiticket/knowledge/service/KnowledgeIngestionServiceTest.java` using hand-written fakes instead of Mockito:

```java
package com.example.aiticket.knowledge.service;

import com.example.aiticket.ai.embedding.EmbeddingClient;
import com.example.aiticket.ai.embedding.EmbeddingResult;
import com.example.aiticket.config.AiProviderProperties;
import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.chunk.ParagraphTextChunker;
import com.example.aiticket.knowledge.domain.KnowledgeChunkDraft;
import com.example.aiticket.knowledge.domain.KnowledgeDocument;
import com.example.aiticket.knowledge.domain.KnowledgeParseStatus;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import com.example.aiticket.knowledge.mapper.KnowledgeDocumentMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeIngestionServiceTest {
    @Test
    void chunksEmbedsAndMarksDocumentSuccess() {
        FakeDocumentMapper documentMapper = new FakeDocumentMapper();
        FakeChunkMapper chunkMapper = new FakeChunkMapper();
        KnowledgeIngestionService service = new KnowledgeIngestionService(
                documentMapper,
                chunkMapper,
                new ParagraphTextChunker(20, 5),
                new FakeEmbeddingClient(),
                knowledgeProperties(),
                aiProperties()
        );

        service.ingestText(10L, "测试文档", 1L, "012345678901234567890123456789");

        assertThat(documentMapper.statuses).containsExactly("PARSING", "PARSE_SUCCESS");
        assertThat(chunkMapper.deletedDocumentIds).containsExactly(10L);
        assertThat(chunkMapper.inserted).hasSize(2);
        assertThat(chunkMapper.inserted.get(0).documentId()).isEqualTo(10L);
        assertThat(chunkMapper.inserted.get(0).sourceTitle()).isEqualTo("测试文档");
        assertThat(chunkMapper.inserted.get(0).vectorLiteral()).startsWith("[");
    }

    private static KnowledgeProperties knowledgeProperties() {
        return new KnowledgeProperties();
    }

    private static AiProviderProperties aiProperties() {
        AiProviderProperties properties = new AiProviderProperties();
        properties.getEmbedding().setDimensions(1024);
        return properties;
    }

    private static final class FakeEmbeddingClient implements EmbeddingClient {
        @Override
        public EmbeddingResult embed(String text) {
            return embedBatch(List.of(text)).getFirst();
        }

        @Override
        public List<EmbeddingResult> embedBatch(List<String> texts) {
            return texts.stream()
                    .map(text -> new EmbeddingResult(text, vector(), 1024))
                    .toList();
        }

        private static List<Float> vector() {
            List<Float> values = new ArrayList<>();
            for (int i = 0; i < 1024; i++) {
                values.add(i == 0 ? 1.0f : 0.0f);
            }
            return values;
        }
    }

    private static final class FakeDocumentMapper implements KnowledgeDocumentMapper {
        private final List<String> statuses = new ArrayList<>();

        @Override
        public int insertTextDocument(String title, Long categoryId, long fileSize, Long uploadedBy) {
            return 1;
        }

        @Override
        public KnowledgeDocument findLatestByTitleAndUploader(String title, Long uploadedBy) {
            return findById(10L);
        }

        @Override
        public KnowledgeDocument findById(Long id) {
            return new KnowledgeDocument(id, "测试文档", 1L, "测试文档", null, "TEXT", 30L, true,
                    KnowledgeParseStatus.PENDING_PARSE, null, 0, 1L, LocalDateTime.now(), LocalDateTime.now(), false);
        }

        @Override
        public List<KnowledgeDocument> findRecent(int limit) {
            return List.of();
        }

        @Override
        public int updateEnabled(Long id, int enabled) {
            return 1;
        }

        @Override
        public int updateParseStatus(Long id, KnowledgeParseStatus parseStatus, String parseError) {
            statuses.add(parseStatus.name());
            return 1;
        }

        @Override
        public int markParseFailed(Long id, String parseError, int maxRetryCount) {
            statuses.add("FAILED:" + parseError);
            return 1;
        }

        @Override
        public int resetForRetry(Long id) {
            return 1;
        }
    }

    private static final class FakeChunkMapper implements KnowledgeChunkMapper {
        private final List<Long> deletedDocumentIds = new ArrayList<>();
        private final List<KnowledgeChunkDraft> inserted = new ArrayList<>();

        @Override
        public int deleteByDocumentId(Long documentId) {
            deletedDocumentIds.add(documentId);
            return 1;
        }

        @Override
        public int insertBatchNonEmpty(List<KnowledgeChunkDraft> chunks) {
            inserted.addAll(chunks);
            return chunks.size();
        }

        @Override
        public List<com.example.aiticket.knowledge.domain.KnowledgeChunk> findByDocumentId(Long documentId) {
            return List.of();
        }

        @Override
        public List<com.example.aiticket.knowledge.domain.KnowledgeSearchResult> search(String queryVectorLiteral, Long categoryId, double minSimilarity, int limit) {
            return List.of();
        }
    }
}
```

- [ ] **Step 2: Run ingestion test and verify it fails**

Run:

```bash
cd backend
mvn test -Dtest=KnowledgeIngestionServiceTest
```

Expected: compilation fails because services do not exist.

- [ ] **Step 3: Implement document service and queue adapter**

Create `KnowledgeParseQueue` and Redis adapter:

```java
package com.example.aiticket.knowledge.queue;

public interface KnowledgeParseQueue {
    void enqueueParseAndEmbed(Long documentId, int retryCount);
}
```

```java
package com.example.aiticket.knowledge.queue;

import com.example.aiticket.config.KnowledgeProperties;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class RedisKnowledgeParseQueue implements KnowledgeParseQueue {
    private final StringRedisTemplate redisTemplate;
    private final KnowledgeProperties properties;

    public RedisKnowledgeParseQueue(StringRedisTemplate redisTemplate, KnowledgeProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void enqueueParseAndEmbed(Long documentId, int retryCount) {
        Map<String, String> body = Map.of(
                "documentId", String.valueOf(documentId),
                "action", "PARSE_AND_EMBED",
                "retryCount", String.valueOf(retryCount),
                "createdAt", Instant.now().toString()
        );
        redisTemplate.opsForStream().add(MapRecord.create(properties.getQueue().getStreamKey(), body));
    }
}
```

Create `KnowledgeDocumentService` with methods:

```java
package com.example.aiticket.knowledge.service;

import com.example.aiticket.knowledge.domain.KnowledgeDocument;
import com.example.aiticket.knowledge.mapper.KnowledgeDocumentMapper;
import com.example.aiticket.knowledge.queue.KnowledgeParseQueue;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class KnowledgeDocumentService {
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeParseQueue parseQueue;

    public KnowledgeDocumentService(KnowledgeDocumentMapper documentMapper, KnowledgeParseQueue parseQueue) {
        this.documentMapper = documentMapper;
        this.parseQueue = parseQueue;
    }

    public Long createTextDocument(String title, Long categoryId, String content, Long uploadedBy) {
        int fileSize = content == null ? 0 : content.getBytes(StandardCharsets.UTF_8).length;
        documentMapper.insertTextDocument(title, categoryId, fileSize, uploadedBy);
        KnowledgeDocument created = documentMapper.findLatestByTitleAndUploader(title, uploadedBy);
        parseQueue.enqueueParseAndEmbed(created.id(), 0);
        return created.id();
    }

    public KnowledgeDocument getDocument(Long id) {
        return documentMapper.findById(id);
    }

    public List<KnowledgeDocument> listRecent(int limit) {
        return documentMapper.findRecent(limit);
    }

    public void setEnabled(Long id, boolean enabled) {
        documentMapper.updateEnabled(id, enabled ? 1 : 0);
    }

    public void resetForRetry(Long id) {
        documentMapper.resetForRetry(id);
        parseQueue.enqueueParseAndEmbed(id, 0);
    }
}
```

- [ ] **Step 4: Implement ingestion service**

Create `backend/src/main/java/com/example/aiticket/knowledge/service/KnowledgeIngestionService.java`:

```java
package com.example.aiticket.knowledge.service;

import com.example.aiticket.ai.embedding.EmbeddingClient;
import com.example.aiticket.ai.embedding.EmbeddingResult;
import com.example.aiticket.config.AiProviderProperties;
import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.chunk.TextChunker;
import com.example.aiticket.knowledge.domain.KnowledgeChunkDraft;
import com.example.aiticket.knowledge.domain.KnowledgeParseStatus;
import com.example.aiticket.knowledge.domain.TextChunk;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import com.example.aiticket.knowledge.mapper.KnowledgeDocumentMapper;
import com.example.aiticket.vector.OracleVectorLiteral;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class KnowledgeIngestionService {
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final TextChunker textChunker;
    private final EmbeddingClient embeddingClient;
    private final KnowledgeProperties knowledgeProperties;
    private final AiProviderProperties aiProviderProperties;

    public KnowledgeIngestionService(KnowledgeDocumentMapper documentMapper,
                                     KnowledgeChunkMapper chunkMapper,
                                     TextChunker textChunker,
                                     EmbeddingClient embeddingClient,
                                     KnowledgeProperties knowledgeProperties,
                                     AiProviderProperties aiProviderProperties) {
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.textChunker = textChunker;
        this.embeddingClient = embeddingClient;
        this.knowledgeProperties = knowledgeProperties;
        this.aiProviderProperties = aiProviderProperties;
    }

    @Transactional
    public void ingestText(Long documentId, String title, Long categoryId, String text) {
        documentMapper.updateParseStatus(documentId, KnowledgeParseStatus.PARSING, null);
        try {
            List<TextChunk> chunks = textChunker.chunk(text);
            List<String> texts = chunks.stream().map(TextChunk::content).toList();
            List<EmbeddingResult> embeddings = embeddingClient.embedBatch(texts);
            if (embeddings.size() != chunks.size()) {
                throw new IllegalStateException("embedding result count does not match chunk count");
            }

            List<KnowledgeChunkDraft> drafts = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                TextChunk chunk = chunks.get(i);
                EmbeddingResult embedding = embeddings.get(i);
                if (embedding.vector().size() != aiProviderProperties.getEmbedding().getDimensions()) {
                    throw new IllegalStateException("embedding dimensions mismatch");
                }
                drafts.add(new KnowledgeChunkDraft(
                        documentId,
                        categoryId,
                        chunk.chunkIndex(),
                        chunk.content(),
                        sha256(chunk.content()),
                        title,
                        null,
                        OracleVectorLiteral.from(embedding.vector())
                ));
            }

            chunkMapper.deleteByDocumentId(documentId);
            if (!drafts.isEmpty()) {
                chunkMapper.insertBatch(drafts);
            }
            documentMapper.updateParseStatus(documentId, KnowledgeParseStatus.PARSE_SUCCESS, null);
        } catch (RuntimeException ex) {
            documentMapper.markParseFailed(documentId, ex.getMessage(), knowledgeProperties.getParse().getMaxRetryCount());
            throw ex;
        }
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
```

- [ ] **Step 5: Write and implement retrieval service**

Create `backend/src/test/java/com/example/aiticket/knowledge/service/KnowledgeRetrievalServiceTest.java` with a fake `EmbeddingClient` and fake mapper asserting that:

```java
retrievalService.search("如何重置密码", null, null, null)
```

uses default `topK = 5`, default `minSimilarity = 0.70`, calls `embed("如何重置密码")`, and passes a vector literal starting with `[` into `KnowledgeChunkMapper.search`.

Then create `backend/src/main/java/com/example/aiticket/knowledge/service/KnowledgeRetrievalService.java`:

```java
package com.example.aiticket.knowledge.service;

import com.example.aiticket.ai.embedding.EmbeddingClient;
import com.example.aiticket.ai.embedding.EmbeddingResult;
import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import com.example.aiticket.vector.OracleVectorLiteral;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeRetrievalService {
    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingClient embeddingClient;
    private final KnowledgeProperties properties;

    public KnowledgeRetrievalService(KnowledgeChunkMapper chunkMapper,
                                     EmbeddingClient embeddingClient,
                                     KnowledgeProperties properties) {
        this.chunkMapper = chunkMapper;
        this.embeddingClient = embeddingClient;
        this.properties = properties;
    }

    public List<KnowledgeSearchResult> search(String query, Long categoryId, Integer topK, Double minSimilarity) {
        int resolvedTopK = topK == null ? properties.getRetrieval().getTopK() : topK;
        double resolvedMinSimilarity = minSimilarity == null
                ? properties.getRetrieval().getMinSimilarity()
                : minSimilarity;
        EmbeddingResult embedding = embeddingClient.embed(query);
        return chunkMapper.search(
                OracleVectorLiteral.from(embedding.vector()),
                categoryId,
                resolvedMinSimilarity,
                resolvedTopK
        );
    }
}
```

- [ ] **Step 6: Run focused tests**

Run:

```bash
cd backend
mvn test -Dtest=KnowledgeIngestionServiceTest,KnowledgeRetrievalServiceTest,ParagraphTextChunkerTest
```

Expected: tests pass.

- [ ] **Step 7: Commit Task 4**

Run:

```bash
git add backend/src/main/java/com/example/aiticket/knowledge/service backend/src/main/java/com/example/aiticket/knowledge/queue backend/src/test/java/com/example/aiticket/knowledge/service docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
git commit -m "feat: add knowledge ingestion services"
```

After commit and review, append `⭐` to the Task 4 heading.

## Task 5: RBAC-Protected Knowledge REST API

**Files:**
- Create request/response records under `backend/src/main/java/com/example/aiticket/knowledge/web/`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/web/KnowledgeDocumentController.java`
- Create: `backend/src/main/java/com/example/aiticket/knowledge/web/KnowledgeSearchController.java`

- [ ] **Step 1: Create web DTOs**

Create:

```java
package com.example.aiticket.knowledge.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTextDocumentRequest(
        @NotBlank @Size(max = 200) String title,
        Long categoryId,
        @NotBlank @Size(max = 200_000) String content
) {
}
```

```java
package com.example.aiticket.knowledge.web;

import com.example.aiticket.knowledge.domain.KnowledgeDocument;

public record DocumentResponse(
        Long id,
        String title,
        Long categoryId,
        Boolean enabled,
        String parseStatus,
        String parseError,
        Integer retryCount
) {
    public static DocumentResponse from(KnowledgeDocument document) {
        return new DocumentResponse(
                document.id(),
                document.title(),
                document.categoryId(),
                document.enabled(),
                document.parseStatus().name(),
                document.parseError(),
                document.retryCount()
        );
    }
}
```

```java
package com.example.aiticket.knowledge.web;

import com.example.aiticket.knowledge.domain.KnowledgeChunk;

public record ChunkResponse(
        Long id,
        Integer chunkIndex,
        String content,
        String contentHash,
        String sourceTitle
) {
    public static ChunkResponse from(KnowledgeChunk chunk) {
        return new ChunkResponse(chunk.id(), chunk.chunkIndex(), chunk.content(), chunk.contentHash(), chunk.sourceTitle());
    }
}
```

```java
package com.example.aiticket.knowledge.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SearchKnowledgeRequest(
        @NotBlank String query,
        Long categoryId,
        @Min(1) @Max(20) Integer topK,
        @Min(0) @Max(1) Double minSimilarity
) {
}
```

```java
package com.example.aiticket.knowledge.web;

import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;

public record SearchKnowledgeResponse(
        Long chunkId,
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String sourceTitle,
        Double distance,
        Double similarity
) {
    public static SearchKnowledgeResponse from(KnowledgeSearchResult result) {
        return new SearchKnowledgeResponse(
                result.chunkId(),
                result.documentId(),
                result.categoryId(),
                result.chunkIndex(),
                result.content(),
                result.sourceTitle(),
                result.distance(),
                result.similarity()
        );
    }
}
```

- [ ] **Step 2: Implement document controller**

Create `backend/src/main/java/com/example/aiticket/knowledge/web/KnowledgeDocumentController.java`:

```java
package com.example.aiticket.knowledge.web;

import com.example.aiticket.common.api.ApiResponse;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import com.example.aiticket.knowledge.service.KnowledgeDocumentService;
import com.example.aiticket.knowledge.service.KnowledgeIngestionService;
import com.example.aiticket.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kb/documents")
public class KnowledgeDocumentController {
    private final KnowledgeDocumentService documentService;
    private final KnowledgeIngestionService ingestionService;
    private final KnowledgeChunkMapper chunkMapper;

    public KnowledgeDocumentController(KnowledgeDocumentService documentService,
                                       KnowledgeIngestionService ingestionService,
                                       KnowledgeChunkMapper chunkMapper) {
        this.documentService = documentService;
        this.ingestionService = ingestionService;
        this.chunkMapper = chunkMapper;
    }

    @PostMapping("/text")
    @PreAuthorize("hasAuthority('knowledge:document:upload')")
    public ApiResponse<DocumentResponse> createTextDocument(@Valid @RequestBody CreateTextDocumentRequest request,
                                                            @AuthenticationPrincipal AuthenticatedUser user) {
        Long categoryId = request.categoryId() == null ? 1L : request.categoryId();
        Long documentId = documentService.createTextDocument(request.title(), categoryId, request.content(), user.userId());
        ingestionService.ingestText(documentId, request.title(), categoryId, request.content());
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(documentId)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<List<DocumentResponse>> listDocuments() {
        return ApiResponse.ok(documentService.listRecent(100).stream().map(DocumentResponse::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<DocumentResponse> getDocument(@PathVariable Long id) {
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('knowledge:document:manage')")
    public ApiResponse<DocumentResponse> enable(@PathVariable Long id) {
        documentService.setEnabled(id, true);
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('knowledge:document:manage')")
    public ApiResponse<DocumentResponse> disable(@PathVariable Long id) {
        documentService.setEnabled(id, false);
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @PostMapping("/{id}/retry-parse")
    @PreAuthorize("hasAuthority('knowledge:document:manage')")
    public ApiResponse<DocumentResponse> retryParse(@PathVariable Long id) {
        documentService.resetForRetry(id);
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @GetMapping("/{id}/chunks")
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<List<ChunkResponse>> chunks(@PathVariable Long id) {
        return ApiResponse.ok(chunkMapper.findByDocumentId(id).stream().map(ChunkResponse::from).toList());
    }
}
```

- [ ] **Step 3: Implement search controller**

Create `backend/src/main/java/com/example/aiticket/knowledge/web/KnowledgeSearchController.java`:

```java
package com.example.aiticket.knowledge.web;

import com.example.aiticket.common.api.ApiResponse;
import com.example.aiticket.knowledge.service.KnowledgeRetrievalService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
public class KnowledgeSearchController {
    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeSearchController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<List<SearchKnowledgeResponse>> search(@Valid @RequestBody SearchKnowledgeRequest request) {
        return ApiResponse.ok(retrievalService.search(
                        request.query(),
                        request.categoryId(),
                        request.topK(),
                        request.minSimilarity()
                ).stream()
                .map(SearchKnowledgeResponse::from)
                .toList());
    }
}
```

- [ ] **Step 4: Run compile and unit tests**

Run:

```bash
cd backend
mvn test
```

Expected: all unit tests pass.

- [ ] **Step 5: Commit Task 5**

Run:

```bash
git add backend/src/main/java/com/example/aiticket/knowledge/web docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
git commit -m "feat: add knowledge REST API"
```

After commit and review, append `⭐` to the Task 5 heading.

## Task 6: Live Oracle, Redis, Auth, and SiliconFlow Verification

**Files:**
- Create: `docs/spikes/knowledge-base-vector-retrieval.md`
- Modify: `docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md`

- [ ] **Step 1: Verify Docker services are running**

Run:

```bash
docker compose ps
```

Expected: Oracle and Redis services are healthy or running. If Docker access requires approval, request escalation.

- [ ] **Step 2: Run Flyway migration through application startup**

Run with the user's local secret file loaded by the shell, without printing the key:

```bash
cd backend
set -a
. /private/tmp/ai-ticket-secrets/siliconflow.env
set +a
mvn spring-boot:run
```

Expected:

1. Application starts on port `8080`.
2. Flyway applies `V3__knowledge_base.sql`.
3. No secret value is printed by the application logs.

- [ ] **Step 3: Login as admin**

Run in another terminal:

```bash
curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin_123456"}'
```

Expected: response contains a JWT token and permissions including:

```text
knowledge:document:upload
knowledge:document:manage
knowledge:document:view
```

- [ ] **Step 4: Create and ingest a demo text document**

Run with the token from Step 3:

```bash
curl -s http://localhost:8080/api/kb/documents/text \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "密码重置操作指南",
    "categoryId": 1,
    "content": "当用户忘记密码时，可以在登录页点击忘记密码，输入绑定手机号并完成验证码校验。校验通过后，系统允许用户设置新密码。管理员也可以在用户权限管理页面为用户重置临时密码。临时密码首次登录后必须修改。"
  }'
```

Expected: response `data.parseStatus` is `PARSE_SUCCESS`.

- [ ] **Step 5: Search the knowledge base**

Run:

```bash
curl -s http://localhost:8080/api/kb/search \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"query":"忘记密码怎么处理","topK":5,"minSimilarity":0.1}'
```

Expected:

1. Response contains at least one result.
2. The top result `sourceTitle` is `密码重置操作指南`.
3. The response does not contain raw embedding vectors.

- [ ] **Step 6: Verify RBAC denial for ordinary user**

Login as `user / Admin_123456`, then run:

```bash
curl -i http://localhost:8080/api/kb/documents/text \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"无权限上传","content":"普通用户不应能上传知识库文档。"}'
```

Expected: HTTP `403`.

- [ ] **Step 7: Record spike report**

Create `docs/spikes/knowledge-base-vector-retrieval.md`:

```markdown
# Knowledge Base Vector Retrieval Spike

Date: 2026-06-19

## Verified

- Oracle Flyway migration `V3__knowledge_base.sql` created `kb_document` and `kb_chunk` with `VECTOR(1024, FLOAT32)`.
- Admin login can create a text knowledge document.
- The ingestion path chunks text, calls SiliconFlow embeddings, writes vectors with `TO_VECTOR(...)`, and marks the document `PARSE_SUCCESS`.
- `/api/kb/search` embeds the query and retrieves the expected top chunk through `VECTOR_DISTANCE(..., COSINE)`.
- Ordinary user token cannot upload knowledge documents.
- API responses do not expose raw vectors or API keys.

## Commands

Record sanitized command summaries and response shapes here. Do not paste API keys or full JWT values.

## Notes

Phase 3 keeps multipart parsing, Tika, RAG answer generation, SSE, AI message persistence, and ticket transfer for later phases.
```

Fill the `Commands` section with sanitized evidence from Steps 1-6.

- [ ] **Step 8: Commit Task 6**

Run:

```bash
git add docs/spikes/knowledge-base-vector-retrieval.md docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
git commit -m "docs: record knowledge retrieval verification"
```

After commit and review, append `⭐` to the Task 6 heading.

## Task 7: Final Verification and Project Plan Alignment

**Files:**
- Modify: `docs/superpowers/specs/2026-06-19-ai-knowledge-ticket-v1-project-plan.md` only if a concise progress note is needed.
- Modify: `docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md`

- [ ] **Step 1: Run all unit tests**

Run:

```bash
cd backend
mvn test
```

Expected: all tests pass.

- [ ] **Step 2: Inspect git diff**

Run:

```bash
git status --short
git diff --stat
git diff -- backend/src/main/java/com/example/aiticket/knowledge backend/src/main/resources/db/migration/V3__knowledge_base.sql backend/src/main/resources/mapper/KnowledgeChunkMapper.xml backend/src/main/resources/mapper/KnowledgeDocumentMapper.xml
```

Expected:

1. Only Phase 3 files are changed.
2. No API keys, JWTs, or raw secrets are present.
3. No unrelated user changes are reverted.

- [ ] **Step 3: Confirm scope alignment**

Check against:

```text
docs/superpowers/specs/2026-06-19-ai-knowledge-ticket-v1-project-plan.md sections 6.2, 6.3, 6.6, 7.2, 8.2, 8.3
docs/superpowers/specs/2026-06-19-ai-knowledge-ticket-system-design.md sections 3.2, 4.5, 4.6, 5.2, 6.1, 7.2
```

Confirm:

1. Knowledge metadata and chunks exist.
2. Parse status state machine exists.
3. Chunking is paragraph-first with length fallback and overlap.
4. Embedding provider remains SiliconFlow through `EmbeddingClient`.
5. Oracle vector storage and Top-K retrieval work.
6. Redis Stream enqueue foundation exists.
7. Full RAG answer generation remains outside Phase 3.

- [ ] **Step 4: Commit final plan progress markings**

If any task stars were added after prior commits, run:

```bash
git add docs/superpowers/plans/2026-06-19-knowledge-base-vector-retrieval-implementation-plan.md
git commit -m "docs: mark knowledge retrieval plan progress"
```

- [ ] **Step 5: Final report**

Final response must include:

```text
Implemented Phase 3 Knowledge Base Vector Retrieval backend foundation.
Unit verification: mvn test
Live verification: admin create text document, SiliconFlow embedding ingestion, Oracle vector search, RBAC denial
Main remaining scope: Phase 4 RAG answer generation and AI chat persistence
```

After commit and review, append `⭐` to the Task 7 heading if not already marked.

## Review Checklist

Before calling the phase complete:

1. `mvn test` passes.
2. Live verification proves Oracle `VECTOR(1024, FLOAT32)` writes and searches real SiliconFlow vectors in `kb_chunk`.
3. `/api/kb/search` filters to enabled, non-deleted, `PARSE_SUCCESS` documents.
4. Raw vectors are not returned by API responses.
5. API keys are never printed, committed, or pasted into docs.
6. Knowledge endpoints require JWT and the intended permission codes.
7. Completed task headings in this plan have `⭐`.
8. The final git status is clean or contains only intentionally uncommitted user changes.
