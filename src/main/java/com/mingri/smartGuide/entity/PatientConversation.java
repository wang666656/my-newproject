package com.mingri.smartGuide.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "patient_conversation")
public class PatientConversation {
    @Id
    @Column(name = "conversation_id")
    private UUID conversationID;

    @Column(name = "outpatientid")
    private String outpatientID;

    private String name;
    private Integer age;
    private String gender;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}