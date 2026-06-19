package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.LicenseAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Журнал действий над лицензиями (создание, активация и т.д.). */
@Entity
@Getter
@Setter
@Table(name = "license_activation_log",
        indexes = {@Index(name = "idx_lic_log_license", columnList = "license_id")})
public class LicenseActivationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "license_id")
    private Long licenseId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private LicenseAction action;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
