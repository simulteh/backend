package com.simul_tech.netgenius.configurators;

import com.simul_tech.netgenius.repositories.RefreshTokenRepository;
import com.simul_tech.netgenius.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanUpScheduler {

    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?") // Каждый день в 3:00
    public void cleanupExpiredTokens() {
        try {
            int deletedCount = tokenRepository.deleteExpired();
            int deletedRefresh = refreshTokenRepository.deleteExpired();
            log.info("Cleaned up {} verify/reset tokens and {} refresh tokens", deletedCount, deletedRefresh);
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens", e);
        }
    }
}