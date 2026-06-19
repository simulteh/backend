package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Заявка студента на смену вуза. */
@Entity
@Getter
@Setter
@Table(name = "university_change_requests",
        indexes = {@Index(name = "idx_uni_change_student", columnList = "student_id")})
public class UniversityChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "new_university_id", nullable = false)
    private Long newUniversityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "document_id")
    private Long documentId;

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
