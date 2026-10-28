package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.impls.TokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/** Доставка ссылок подтверждения/сброса после фиксации бизнес-транзакции. */
@Entity
@Getter
@Setter
@Table(name = "mail_outbox", indexes = {
        @Index(name = "idx_mail_due", columnList = "status,next_attempt_at"),
        @Index(name = "idx_mail_user", columnList = "user_id")})
public class MailOutbox {
    public enum Status { PENDING, SENT, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String recipient;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TokenType type;
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;
    @Column(name = "encrypted_token", length = 256)
    private String encryptedToken;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;
    @Column(name = "sent_at")
    private Instant sentAt;

    public String encryptionContext() {
        return userId + ":" + type + ":" + recipient + ":" + tokenHash;
    }
}
