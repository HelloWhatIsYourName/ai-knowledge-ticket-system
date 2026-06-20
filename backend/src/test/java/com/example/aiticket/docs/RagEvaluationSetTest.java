package com.example.aiticket.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RagEvaluationSetTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void ragEvaluationSetHasEnoughStructuredCasesForFirstVersionMetrics() throws Exception {
        Path datasetPath = Path.of("../docs/evaluation/rag-evaluation-set.json");
        assertThat(datasetPath).exists();

        JsonNode cases = objectMapper.readTree(Files.readString(datasetPath));
        assertThat(cases.isArray()).isTrue();
        assertThat(cases).hasSize(20);

        Set<String> categories = new HashSet<>();
        int transferCases = 0;
        for (JsonNode evaluationCase : cases) {
            assertTextField(evaluationCase, "id");
            assertTextField(evaluationCase, "category");
            assertTextField(evaluationCase, "question");
            assertTextField(evaluationCase, "expectedSourceHint");
            assertThat(evaluationCase.path("expectedKeywords").isArray()).isTrue();
            assertThat(evaluationCase.path("expectedKeywords")).isNotEmpty();
            assertThat(evaluationCase.path("shouldTransfer").isBoolean()).isTrue();

            categories.add(evaluationCase.path("category").asText());
            if (evaluationCase.path("shouldTransfer").asBoolean()) {
                transferCases++;
            }
        }

        assertThat(categories).hasSizeGreaterThanOrEqualTo(5);
        assertThat(transferCases).isGreaterThanOrEqualTo(5);
    }

    @Test
    void ragEvaluationGuideDefinesManualScoringRules() throws Exception {
        Path guidePath = Path.of("../docs/evaluation/rag-evaluation-set.md");
        assertThat(guidePath).exists();
        String guide = Files.readString(guidePath);

        assertThat(guide).contains("检索命中");
        assertThat(guide).contains("回答有用率");
        assertThat(guide).contains("误转工单率");
        assertThat(guide).contains("shouldTransfer");
    }

    private void assertTextField(JsonNode node, String fieldName) {
        assertThat(node.path(fieldName).isTextual()).isTrue();
        assertThat(node.path(fieldName).asText()).isNotBlank();
    }
}
