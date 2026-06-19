package com.example.aiticket.ai.rag.service;

import com.example.aiticket.ai.chat.ChatResult;
import com.example.aiticket.ai.rag.domain.RagPolicyDecision;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RagAnswerPolicy {
    public RagPolicyDecision decide(List<KnowledgeSearchResult> results,
                                    ChatResult chatResult,
                                    double minSimilarity) {
        List<KnowledgeSearchResult> safeResults = results == null ? List.of() : results;
        if (safeResults.isEmpty()) {
            return new RagPolicyDecision(false, 0.0, true, "未检索到相关知识片段");
        }

        double topSimilarity = safeResults.stream()
                .map(KnowledgeSearchResult::similarity)
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(0.0);

        if (topSimilarity < minSimilarity) {
            return new RagPolicyDecision(false, round(topSimilarity), true, "知识片段相似度低于阈值");
        }

        if (chatResult != null && !chatResult.canAnswer()) {
            return new RagPolicyDecision(false, round(chatResult.confidence()), true, "模型自评无法可靠回答");
        }

        double modelConfidence = chatResult == null ? 0.7 : chatResult.confidence();
        double confidence = Math.min(topSimilarity, (topSimilarity + modelConfidence) / 2.0);
        return new RagPolicyDecision(true, round(confidence), false, null);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
