package com.example.aiticket.ai.rag.prompt;

import com.example.aiticket.ai.rag.domain.RagPrompt;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagPromptBuilderTest {
    @Test
    void buildsPromptWithNumberedSourcesAndSafetyRules() {
        RagPromptBuilder builder = new RagPromptBuilder();

        RagPrompt prompt = builder.build("忘记密码怎么处理？", List.of(
                new KnowledgeSearchResult(11L, 21L, 1L, 0,
                        "用户可以在登录页点击忘记密码，输入绑定手机号并完成验证码校验。",
                        "密码重置操作指南", 0.2, 0.8),
                new KnowledgeSearchResult(12L, 22L, 1L, 0,
                        "管理员可以在用户权限管理页面为用户重置临时密码。",
                        "管理员操作指南", 0.25, 0.75)
        ));

        assertThat(prompt.prompt()).contains("只允许基于本次召回的授权知识片段回答");
        assertThat(prompt.prompt()).contains("不得编造制度、流程、价格、时间、联系方式或处理结论");
        assertThat(prompt.prompt()).contains("[1] 密码重置操作指南");
        assertThat(prompt.prompt()).contains("[2] 管理员操作指南");
        assertThat(prompt.prompt()).contains("用户问题：忘记密码怎么处理？");
        assertThat(prompt.citations()).hasSize(2);
        assertThat(prompt.citations().get(0).citationIndex()).isEqualTo(1);
        assertThat(prompt.citations().get(0).chunkId()).isEqualTo(11L);
        assertThat(prompt.citations().get(0).snippet()).contains("登录页点击忘记密码");
    }

    @Test
    void buildsCannotConfirmPromptWhenNoSourcesAreRetrieved() {
        RagPromptBuilder builder = new RagPromptBuilder();

        RagPrompt prompt = builder.build("公司餐补是多少？", List.of());

        assertThat(prompt.prompt()).contains("本次没有检索到可用知识片段");
        assertThat(prompt.prompt()).contains("请明确说明知识库没有覆盖，无法确认");
        assertThat(prompt.citations()).isEmpty();
    }
}
