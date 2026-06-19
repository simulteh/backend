package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.InvalidTokenException;
import com.simul_tech.netgenius.models.RefreshToken;
import com.simul_tech.netgenius.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Управление refresh-токенами: выпуск, проверка, ротация и отзыв.
 * Время жизни — 30 дней. Хранится только хеш токена.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-ttl-days:30}")
    private long refreshTtlDays;

    @Transactional
    public String issue(Long userId) {
        RefreshToken token = new RefreshToken(userId, Duration.ofDays(refreshTtlDays));
        refreshTokenRepository.save(token);
        return token.getRawToken();
    }

    /** Проверяет refresh-токен, отзывает его и выпускает новый (ротация). Возвращает новый «сырой» токен. */
    @Transactional
    public RotationResult rotate(String rawToken) {
        RefreshToken current = validate(rawToken);
        current.setRevoked(true);
        refreshTokenRepository.save(current);

        RefreshToken next = new RefreshToken(current.getUserId(), Duration.ofDays(refreshTtlDays));
        refreshTokenRepository.save(next);
        return new RotationResult(current.getUserId(), next.getRawToken());
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Refresh-токен не передан");
        }
        RefreshToken token = refreshTokenRepository.findByTokenHash(RefreshToken.hash(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Недействительный refresh-токен"));
        if (!token.isUsable()) {
            throw new InvalidTokenException("Refresh-токен истёк или отозван");
        }
        return token;
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    /** Результат ротации: владелец и новый «сырой» refresh-токен. */
    public record RotationResult(Long userId, String rawToken) {
    }
}
