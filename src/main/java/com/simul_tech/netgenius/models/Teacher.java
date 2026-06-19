package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Профиль преподавателя. Создаётся при подаче заявки на роль TEACHER (verified=false)
 * и подтверждается администратором.
 */
@Entity
@Getter
@Setter
@Table(name = "teachers",
        uniqueConstraints = {@UniqueConstraint(name = "uk_teachers_user", columnNames = "user_id")})
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "workplace")
    private String workplace;

    @Column(name = "position")
    private String position;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
