package com.mingri.smartGuide.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "access_record")
public class AccessRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outpatient_id", nullable = false, length = 50)
    private String outpatientID;

    @Column(name = "access_time", nullable = false)
    private LocalDateTime accessTime;

    @PrePersist
    protected void onCreate() {
        if (accessTime == null) {
            accessTime = LocalDateTime.now();
        }
    }
}