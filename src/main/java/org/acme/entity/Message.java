package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "messages")
public class Message extends PanacheEntity {

    @Column(name = "session_id", nullable = false)
    public String sessionId;

    @Column(name = "user_message", columnDefinition = "TEXT")
    public String userMessage;

    @Column(name = "bot_response", columnDefinition = "TEXT")
    public String botResponse;

    @Column(name = "therapy_type")
    public String therapyType;

    @Column(name = "sentiment")
    public String sentiment;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static List<Message> findBySessionId(String sessionId) {
        return list("sessionId", sessionId);
    }
}