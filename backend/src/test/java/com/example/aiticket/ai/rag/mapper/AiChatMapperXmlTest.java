package com.example.aiticket.ai.rag.mapper;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AiChatMapperXmlTest {
    @Test
    void ragChatMigrationDefinesSessionMessageAndCitationTables() throws Exception {
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V4__ai_rag_chat.sql"));

        assertThat(migration).contains("CREATE TABLE ai_session");
        assertThat(migration).contains("CREATE TABLE ai_message");
        assertThat(migration).contains("CREATE TABLE ai_message_citation");
        assertThat(migration).contains("CONSTRAINT ck_ai_message_role CHECK (role IN ('USER', 'ASSISTANT'))");
        assertThat(migration).contains("CONSTRAINT fk_ai_citation_chunk FOREIGN KEY (chunk_id) REFERENCES kb_chunk(id)");
    }

    @Test
    void aiChatMapperDeclaresCorePersistenceStatements() throws Exception {
        String mapper = Files.readString(Path.of("src/main/resources/mapper/AiChatMapper.xml"));

        assertThat(mapper).contains("insertSession");
        assertThat(mapper).contains("insertMessage");
        assertThat(mapper).contains("insertCitation");
        assertThat(mapper).contains("listOwnedSessions");
        assertThat(mapper).contains("listMessages");
    }
}
