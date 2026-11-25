package com.vasudha.referral_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorId;     // who performed action
    private Long targetId;    // affected user

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    private String metadata;  // optional JSON/string details

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}