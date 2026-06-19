package com.example.aiticket.knowledge.mapper;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeMapperXmlTest {
    @Test
    void nullableOracleParametersDeclareJdbcTypes() throws Exception {
        String documentMapper = Files.readString(Path.of("src/main/resources/mapper/KnowledgeDocumentMapper.xml"));
        String chunkMapper = Files.readString(Path.of("src/main/resources/mapper/KnowledgeChunkMapper.xml"));

        assertThat(documentMapper).contains("#{parseError,jdbcType=VARCHAR}");
        assertThat(chunkMapper).contains("#{chunk.sourcePage,jdbcType=NUMERIC}");
    }
}
