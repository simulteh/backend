package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.VerificationRequestType;
import com.simul_tech.netgenius.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Заявка пользователя на подтверждение роли (студент/преподаватель). */
@Entity
@Getter
@Setter
@Table(name = "verification_requests",
        indexes = {@Index(name = "idx_verif_user", columnList = "user_id")})
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private VerificationRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "comment", length = 1000)
    private String comment;

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
