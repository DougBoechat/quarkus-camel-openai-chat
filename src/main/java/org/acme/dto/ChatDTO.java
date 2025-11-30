package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatDTO {

    public static class ChatRequest {
        @JsonProperty("session_id")
        public String sessionId;

        @JsonProperty("message")
        public String message;

        @JsonProperty("therapy_type")
        public String therapyType; // ayurveda, acupuntura, aromaterapia, etc
    }

    public static class ChatResponse {
        @JsonProperty("session_id")
        public String sessionId;

        @JsonProperty("message")
        public String message;

        @JsonProperty("therapy_type")
        public String therapyType;

        @JsonProperty("suggestions")
        public String[] suggestions;

        @JsonProperty("sentiment")
        public String sentiment;

        @JsonProperty("timestamp")
        public String timestamp;
    }

    public static class TherapyInfo {
        @JsonProperty("type")
        public String type;

        @JsonProperty("description")
        public String description;

        @JsonProperty("benefits")
        public String[] benefits;
    }
}