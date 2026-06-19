package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Связь студент-группа (many-to-many). Студент может состоять в нескольких группах.
 * Уникальность пары (student_id, group_id) гарантируется ограничением.
 */
@Entity
@Getter
@Setter
@Table(name = "student_groups",
        uniqueConstraints = {@UniqueConstraint(name = "uk_student_group", columnNames = {"student_id", "group_id"})},
        indexes = {
                @Index(name = "idx_student_groups_student", columnList = "student_id"),
                @Index(name = "idx_student_groups_group", columnList = "group_id")
        })
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        joinedAt = Instant.now();
    }
}
