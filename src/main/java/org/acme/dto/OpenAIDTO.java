package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OpenAIDTO {

    public static class ChatCompletionRequest {
        @JsonProperty("model")
        public String model;

        @JsonProperty("messages")
        public List<Message> messages;

        @JsonProperty("temperature")
        public Double temperature;

        @JsonProperty("max_tokens")
        public Integer maxTokens;

        @JsonProperty("top_p")
        public Double topP;

        @JsonProperty("frequency_penalty")
        public Double frequencyPenalty;

        @JsonProperty("presence_penalty")
        public Double presencePenalty;
    }

    public static class Message {
        @JsonProperty("role")
        public String role; // system, user, assistant

        @JsonProperty("content")
        public String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ChatCompletionResponse {
        @JsonProperty("id")
        public String id;

        @JsonProperty("object")
        public String object;

        @JsonProperty("created")
        public Long created;

        @JsonProperty("model")
        public String model;

        @JsonProperty("choices")
        public List<Choice> choices;

        @JsonProperty("usage")
        public Usage usage;
    }

    public static class Choice {
        @JsonProperty("index")
        public Integer index;

        @JsonProperty("message")
        public Message message;

        @JsonProperty("finish_reason")
        public String finishReason;
    }

    public static class Usage {
        @JsonProperty("prompt_tokens")
        public Integer promptTokens;

        @JsonProperty("completion_tokens")
        public Integer completionTokens;

        @JsonProperty("total_tokens")
        public Integer totalTokens;
    }
}