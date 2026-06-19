package com.example.aiticket.ai.rag.service;

import com.example.aiticket.ai.chat.ChatResult;
import com.example.aiticket.ai.rag.domain.RagPolicyDecision;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagAnswerPolicyTest {
    @Test
    void suggestsTransferWhenNoKnowledgeIsRetrieved() {
        RagAnswerPolicy policy = new RagAnswerPolicy();

        RagPolicyDecision decision = policy.decide(List.of(), chat(true, 0.9), 0.7);

        assertThat(decision.canAnswer()).isFalse();
        assertThat(decision.confidence()).isEqualTo(0.0);
        assertThat(decision.transferSuggested()).isTrue();
        assertThat(decision.transferReason()).isEqualTo("未检索到相关知识片段");
    }

    @Test
    void suggestsTransferWhenTopSimilarityIsBelowThreshold() {
        RagAnswerPolicy policy = new RagAnswerPolicy();

        RagPolicyDecision decision = policy.decide(List.of(result(0.52)), chat(true, 0.9), 0.7);

        assertThat(decision.canAnswer()).isFalse();
        assertThat(decision.confidence()).isEqualTo(0.52);
        assertThat(decision.transferSuggested()).isTrue();
        assertThat(decision.transferReason()).isEqualTo("知识片段相似度低于阈值");
    }

    @Test
    void acceptsHighSimilarityAnswerAndCombinesConfidenceConservatively() {
        RagAnswerPolicy policy = new RagAnswerPolicy();

        RagPolicyDecision decision = policy.decide(List.of(result(0.86)), chat(true, 0.74), 0.7);

        assertThat(decision.canAnswer()).isTrue();
        assertThat(decision.confidence()).isEqualTo(0.8);
        assertThat(decision.transferSuggested()).isFalse();
        assertThat(decision.transferReason()).isNull();
    }

    @Test
    void modelSelfRefusalSuggestsTransferEvenWhenRetrievalIsStrong() {
        RagAnswerPolicy policy = new RagAnswerPolicy();

        RagPolicyDecision decision = policy.decide(List.of(result(0.9)), chat(false, 0.2), 0.7);

        assertThat(decision.canAnswer()).isFalse();
        assertThat(decision.confidence()).isEqualTo(0.2);
        assertThat(decision.transferSuggested()).isTrue();
        assertThat(decision.transferReason()).isEqualTo("模型自评无法可靠回答");
    }

    private ChatResult chat(boolean canAnswer, double confidence) {
        return new ChatResult("deepseek-chat", "answer", canAnswer, confidence, null);
    }

    private KnowledgeSearchResult result(double similarity) {
        return new KnowledgeSearchResult(1L, 2L, 3L, 0, "content", "title",
                1.0 - similarity, similarity);
    }
}
