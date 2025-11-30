package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class ClaudeDTO {

    /**
     * Requisição para a API do Claude
     */
    @JsonInclude(JsonInclude.Include.NON_NULL) // Ignora campos null no JSON
    public static class MessageRequest {
        @JsonProperty("model")
        public String model;

        @JsonProperty("max_tokens")
        public Integer maxTokens;

        @JsonProperty("temperature")
        public Double temperature;

        @JsonProperty("system")
        public String system;

        @JsonProperty("messages")
        public List<Message> messages;

        @JsonProperty("top_p")
        public Double topP;

        // ❌ REMOVIDO - Claude API não aceita top_k
        // @JsonProperty("top_k")
        // public Integer topK;

        @JsonProperty("stop_sequences")
        public List<String> stopSequences;
    }

    /**
     * Mensagem individual (user ou assistant)
     */
    public static class Message {
        @JsonProperty("role")
        public String role;

        @JsonProperty("content")
        public Object content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public Message(String role, List<ContentBlock> content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * Bloco de conteúdo (texto, imagem, etc)
     */
    public static class ContentBlock {
        @JsonProperty("type")
        public String type;

        @JsonProperty("text")
        public String text;

        @JsonProperty("source")
        public ImageSource source;

        public ContentBlock() {}

        public ContentBlock(String text) {
            this.type = "text";
            this.text = text;
        }
    }

    /**
     * Fonte de imagem (para multimodal)
     */
    public static class ImageSource {
        @JsonProperty("type")
        public String type;

        @JsonProperty("media_type")
        public String mediaType;

        @JsonProperty("data")
        public String data;
    }

    /**
     * Resposta da API do Claude
     */
    public static class MessageResponse {
        @JsonProperty("id")
        public String id;

        @JsonProperty("type")
        public String type;

        @JsonProperty("role")
        public String role;

        @JsonProperty("content")
        public List<ContentBlock> content;

        @JsonProperty("model")
        public String model;

        @JsonProperty("stop_reason")
        public String stopReason;

        @JsonProperty("stop_sequence")
        public String stopSequence;

        @JsonProperty("usage")
        public Usage usage;
    }

    /**
     * Informações de uso de tokens
     */
    public static class Usage {
        @JsonProperty("input_tokens")
        public Integer inputTokens;

        @JsonProperty("output_tokens")
        public Integer outputTokens;
    }

    /**
     * Erro da API
     */
    public static class ErrorResponse {
        @JsonProperty("type")
        public String type;

        @JsonProperty("error")
        public Error error;

        public static class Error {
            @JsonProperty("type")
            public String type;

            @JsonProperty("message")
            public String message;
        }
    }
}