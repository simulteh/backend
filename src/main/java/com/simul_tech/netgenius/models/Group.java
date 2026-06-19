package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Учебная группа. Всегда принадлежит вузу и преподавателю. */
@Entity
@Getter
@Setter
@Table(name = "groups",
        indexes = {@Index(name = "idx_groups_teacher", columnList = "teacher_id")})
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
