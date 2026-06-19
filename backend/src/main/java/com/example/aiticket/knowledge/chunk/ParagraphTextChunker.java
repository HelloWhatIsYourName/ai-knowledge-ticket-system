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
