package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.utils.TokenGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Duration;
import java.time.Instant;

/**
 * Refresh-токен (время жизни 30 дней). Хранится только хеш; «сырое» значение
 * возвращается клиенту один раз. Поддерживает отзыв (logout) и ротацию.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens",
        uniqueConstraints = {@UniqueConstraint(name = "uk_refresh_hash", columnNames = "token_hash")},
        indexes = {@Index(name = "idx_refresh_user", columnList = "user_id")})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Transient
    private String rawToken;

    public RefreshToken(Long userId, Duration ttl) {
        this.rawToken = TokenGenerator.urlSafe(48);
        this.tokenHash = DigestUtils.sha256Hex(rawToken);
        this.userId = userId;
        this.issuedAt = Instant.now();
        this.expiresAt = Instant.now().plus(ttl);
        this.revoked = false;
    }

    public static String hash(String rawToken) {
        return DigestUtils.sha256Hex(rawToken);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isUsable() {
        return !revoked && !isExpired();
    }
}
