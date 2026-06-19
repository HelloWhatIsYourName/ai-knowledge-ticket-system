package com.example.aiticket.ai.chat;

import com.example.aiticket.config.AiProviderProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiCompatibleChatClientTest {
    @Test
    void sendsChatCompletionRequestAndExtractsAnswerContent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient(properties(), builder);

        server.expect(once(), requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(method(POST))
                .andExpect(header(AUTHORIZATION, "Bearer chat-key"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("deepseek-chat"))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("请回答密码重置流程"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "role": "assistant",
                                "content": "可以在登录页点击忘记密码。"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        ChatResult result = client.chat("请回答密码重置流程");

        assertThat(result.model()).isEqualTo("deepseek-chat");
        assertThat(result.content()).isEqualTo("可以在登录页点击忘记密码。");
        assertThat(result.canAnswer()).isTrue();
        assertThat(result.confidence()).isEqualTo(0.7);
        assertThat(result.reason()).isNull();
        server.verify();
    }

    @Test
    void parsesStructuredSelfAssessmentWhenProviderReturnsJsonContent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient(properties(), builder);

        server.expect(once(), requestTo("https://api.deepseek.com/chat/completions"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "role": "assistant",
                                "content": "{\\"answer\\":\\"知识库没有覆盖该问题。\\",\\"can_answer\\":false,\\"confidence\\":0.22,\\"reason\\":\\"未检索到相关知识片段\\"}"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        ChatResult result = client.chat("公司餐补是多少？");

        assertThat(result.content()).isEqualTo("知识库没有覆盖该问题。");
        assertThat(result.canAnswer()).isFalse();
        assertThat(result.confidence()).isEqualTo(0.22);
        assertThat(result.reason()).isEqualTo("未检索到相关知识片段");
        server.verify();
    }

    @Test
    void rejectsBlankPromptBeforeCallingProvider() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient(properties(), builder);

        assertThatThrownBy(() -> client.chat(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("prompt must not be blank");

        server.verify();
    }

    @Test
    void rejectsMissingProviderChoices() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient(properties(), builder);

        server.expect(once(), requestTo("https://api.deepseek.com/chat/completions"))
                .andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.chat("hello"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("chat response does not contain an answer");

        server.verify();
    }

    private AiProviderProperties properties() {
        AiProviderProperties properties = new AiProviderProperties();
        AiProviderProperties.Provider chat = new AiProviderProperties.Provider();
        chat.setProvider("deepseek");
        chat.setBaseUrl("https://api.deepseek.com");
        chat.setApiKey("chat-key");
        chat.setModel("deepseek-chat");
        properties.setChat(chat);
        return properties;
    }
}
