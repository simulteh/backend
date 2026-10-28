package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.LabStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Отдельная попытка прохождения лабораторной работы. Количество попыток не ограничено. */
@Entity
@Getter
@Setter
@Table(name = "lab_attempts",
        uniqueConstraints = @UniqueConstraint(name = "uk_attempt_event", columnNames = {"student_id", "event_key"}),
        indexes = {
                @Index(name = "idx_attempts_student", columnList = "student_id"),
                @Index(name = "idx_attempts_lab", columnList = "lab_id")
        })
public class LabAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "lab_id", nullable = false)
    private Long labId;

    /** null у попыток, записанных до появления идемпотентной синхронизации. */
    @Column(name = "event_key", length = 64)
    private String eventKey;

    @Column(name = "payload_hash", length = 64)
    private String payloadHash;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "score")
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LabStatus status = LabStatus.STARTED;

    @Column(name = "client_version")
    private String clientVersion;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
