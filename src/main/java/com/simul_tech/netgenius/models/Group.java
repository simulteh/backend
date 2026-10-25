package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Учебная группа преподавателя: организация текстом или существующий вуз LMS. */
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

    @Column(name = "university_id")
    private Long universityId;

    @Column(name = "organization", length = 255)
    private String organization;

    @Column(name = "invite_code", unique = true, length = 64)
    private String inviteCode;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (inviteCode == null) {
            inviteCode = com.simul_tech.netgenius.utils.TokenGenerator.urlSafe(24);
        }
    }
}
