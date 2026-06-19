package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Профиль студента. Создаётся при подаче заявки на роль STUDENT (verified=false)
 * и подтверждается администратором. Студент принадлежит только одному вузу.
 */
@Entity
@Getter
@Setter
@Table(name = "students",
        uniqueConstraints = {@UniqueConstraint(name = "uk_students_user", columnNames = "user_id")},
        indexes = {@Index(name = "idx_students_university", columnList = "university_id")})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "phone")
    private String phone;

    /** Вуз студента (один). Может быть null до подтверждения/назначения. */
    @Column(name = "university_id")
    private Long universityId;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "speciality")
    private String speciality;

    @Column(name = "direction")
    private String direction;

    @Column(name = "course")
    private Integer course;

    @Column(name = "degree")
    private String degree;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
