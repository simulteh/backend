package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Документ, приложенный к заявке на подтверждение. */
@Entity
@Getter
@Setter
@Table(name = "verification_documents",
        indexes = {@Index(name = "idx_verif_doc_request", columnList = "request_id")})
public class VerificationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @PrePersist
    void onCreate() {
        uploadedAt = Instant.now();
    }
}
