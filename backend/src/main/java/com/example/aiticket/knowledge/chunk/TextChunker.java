package com.example.aiticket.knowledge.chunk;

import com.example.aiticket.knowledge.domain.TextChunk;

import java.util.List;

public interface TextChunker {
    List<TextChunk> chunk(String text);
}
