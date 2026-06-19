package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Партия лицензий, выпущенная для вуза. */
@Entity
@Getter
@Setter
@Table(name = "license_batches")
public class LicenseBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long id;

    @Column(name = "university_id")
    private Long universityId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_by_admin")
    private Long createdByAdmin;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
