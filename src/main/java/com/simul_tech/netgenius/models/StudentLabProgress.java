package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.LabStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Агрегированный прогресс студента по лабораторной работе.
 * Основной оценкой считается лучшая попытка (best_score).
 */
@Entity
@Getter
@Setter
@Table(name = "student_lab_progress",
        uniqueConstraints = {@UniqueConstraint(name = "uk_student_lab", columnNames = {"student_id", "lab_id"})},
        indexes = {@Index(name = "idx_progress_student", columnList = "student_id")})
public class StudentLabProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "lab_id", nullable = false)
    private Long labId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LabStatus status = LabStatus.NOT_STARTED;

    /** Лучшая оценка по всем попыткам — основная оценка студента. */
    @Column(name = "best_score")
    private Integer bestScore;

    @Column(name = "attempts_count", nullable = false)
    private int attemptsCount = 0;

    @Column(name = "first_started_at")
    private Instant firstStartedAt;

    @Column(name = "last_completed_at")
    private Instant lastCompletedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
