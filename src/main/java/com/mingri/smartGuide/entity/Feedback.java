package com.mingri.smartGuide.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @Column(name = "message_id", nullable = false)
    private UUID messageID;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationID;

    @Column(name = "outpatientid", nullable = false)
    private String outpatientID;

    @Column(name = "feedback", nullable = false)
    private Boolean feedBack;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
