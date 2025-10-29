package com.simul_tech.netgenius.configurators;


import com.simul_tech.netgenius.repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanUpScheduler {
    private final PasswordResetTokenRepository tokenRepository;

    @Scheduled(cron = "0 0 3 * * ?") // Каждый день в 3:00
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = tokenRepository.deleteByExpiryDateBefore(now);

            if (deletedCount > 0) {
                log.info("Cleaned up {} expired password reset tokens", deletedCount);
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens", e);
        }
    }
}
