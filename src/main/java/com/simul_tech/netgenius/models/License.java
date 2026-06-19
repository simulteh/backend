package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Лицензия на доступ к симулятору. Действует 365 дней с момента активации.
 * Один ключ может быть активирован только один раз.
 */
@Entity
@Getter
@Setter
@Table(name = "licenses",
        uniqueConstraints = {@UniqueConstraint(name = "uk_license_key", columnNames = "license_key")},
        indexes = {@Index(name = "idx_licenses_owner", columnList = "owner_user_id")})
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long id;

    @Column(name = "license_key", nullable = false, unique = true)
    private String licenseKey;

    /** Владелец после активации; до активации null. */
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "issued_by_admin")
    private Long issuedByAdmin;

    /** Партия, в рамках которой выпущена лицензия (если применимо). */
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    /** Активна (выдана и не отозвана). */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public boolean isActivated() {
        return ownerUserId != null && activatedAt != null;
    }
}
