package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Заявка преподавателя на создание вуза. */
@Entity
@Getter
@Setter
@Table(name = "university_creation_requests",
        indexes = {@Index(name = "idx_uni_creation_teacher", columnList = "teacher_id")})
public class UniversityCreationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "university_name", nullable = false)
    private String universityName;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "speciality")
    private String speciality;

    @Column(name = "direction")
    private String direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = VerificationStatus.PENDING;
        }
    }
}
