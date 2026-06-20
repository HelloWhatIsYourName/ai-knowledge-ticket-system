package com.example.aiticket.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DemoCorpusTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void demoCorpusCoversNonTransferEvaluationSourceHints() throws Exception {
        Path corpusPath = Path.of("../docs/demo/v1-demo-corpus.json");
        Path evaluationPath = Path.of("../docs/evaluation/rag-evaluation-set.json");
        assertThat(corpusPath).exists();
        assertThat(evaluationPath).exists();

        JsonNode documents = objectMapper.readTree(Files.readString(corpusPath));
        JsonNode cases = objectMapper.readTree(Files.readString(evaluationPath));

        assertThat(documents.isArray()).isTrue();
        assertThat(documents).hasSizeGreaterThanOrEqualTo(10);

        Set<String> coveredHints = new HashSet<>();
        for (JsonNode document : documents) {
            assertTextField(document, "id");
            assertTextField(document, "title");
            assertThat(document.path("categoryId").isIntegralNumber()).isTrue();
            assertTextField(document, "content");
            assertThat(document.path("sourceHints").isArray()).isTrue();
            assertThat(document.path("sourceHints")).isNotEmpty();

            for (JsonNode hint : document.path("sourceHints")) {
                assertThat(hint.isTextual()).isTrue();
                coveredHints.add(hint.asText());
            }
        }

        for (JsonNode evaluationCase : cases) {
            if (!evaluationCase.path("shouldTransfer").asBoolean()) {
                assertThat(coveredHints)
                        .as("source hint for %s", evaluationCase.path("id").asText())
                        .contains(evaluationCase.path("expectedSourceHint").asText());
            }
        }
    }

    private void assertTextField(JsonNode node, String fieldName) {
        assertThat(node.path(fieldName).isTextual()).isTrue();
        assertThat(node.path(fieldName).asText()).isNotBlank();
    }
}
