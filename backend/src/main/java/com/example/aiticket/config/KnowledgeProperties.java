package com.example.aiticket.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "knowledge")
public class KnowledgeProperties {
    @Valid
    private Chunk chunk = new Chunk();

    @Valid
    private Retrieval retrieval = new Retrieval();

    @Valid
    private Parse parse = new Parse();

    @Valid
    private Queue queue = new Queue();

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public Retrieval getRetrieval() {
        return retrieval;
    }

    public void setRetrieval(Retrieval retrieval) {
        this.retrieval = retrieval;
    }

    public Parse getParse() {
        return parse;
    }

    public void setParse(Parse parse) {
        this.parse = parse;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public static class Chunk {
        @Min(100)
        private int maxChars = 700;

        @Min(0)
        private int overlapChars = 100;

        public int getMaxChars() {
            return maxChars;
        }

        public void setMaxChars(int maxChars) {
            this.maxChars = maxChars;
        }

        public int getOverlapChars() {
            return overlapChars;
        }

        public void setOverlapChars(int overlapChars) {
            this.overlapChars = overlapChars;
        }

        @AssertTrue(message = "overlapChars must be smaller than maxChars")
        public boolean isOverlapSmallerThanMaxChars() {
            return overlapChars < maxChars;
        }
    }

    public static class Retrieval {
        @Min(1)
        @Max(20)
        private int topK = 5;

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private double minSimilarity = 0.70;

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public double getMinSimilarity() {
            return minSimilarity;
        }

        public void setMinSimilarity(double minSimilarity) {
            this.minSimilarity = minSimilarity;
        }
    }

    public static class Parse {
        @Min(0)
        @Max(10)
        private int maxRetryCount = 3;

        public int getMaxRetryCount() {
            return maxRetryCount;
        }

        public void setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }
    }

    public static class Queue {
        @NotBlank
        private String streamKey = "stream:kb:parse";

        @NotBlank
        private String consumerGroup = "kb-parser-group";

        public String getStreamKey() {
            return streamKey;
        }

        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }
    }
}
