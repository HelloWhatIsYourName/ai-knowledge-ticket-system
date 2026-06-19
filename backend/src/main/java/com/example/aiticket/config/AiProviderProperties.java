package com.example.aiticket.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ai")
public class AiProviderProperties {
    @Valid
    private Provider chat = new Provider();

    @Valid
    private EmbeddingProvider embedding = new EmbeddingProvider();

    public Provider getChat() {
        return chat;
    }

    public void setChat(Provider chat) {
        this.chat = chat;
    }

    public EmbeddingProvider getEmbedding() {
        return embedding;
    }

    public void setEmbedding(EmbeddingProvider embedding) {
        this.embedding = embedding;
    }

    public static class Provider {
        @NotBlank
        private String provider;

        @NotBlank
        private String baseUrl;

        private String apiKey;

        @NotBlank
        private String model;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class EmbeddingProvider extends Provider {
        @Min(1)
        private int dimensions = 1024;

        public int getDimensions() {
            return dimensions;
        }

        public void setDimensions(int dimensions) {
            this.dimensions = dimensions;
        }
    }
}
