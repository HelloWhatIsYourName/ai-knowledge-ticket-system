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
