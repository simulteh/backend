package com.simul_tech.netgenius.dto.license;

import com.simul_tech.netgenius.enums.LicenseAction;
import com.simul_tech.netgenius.models.LicenseActivationLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Запись истории действий над лицензиями пользователя (GET /licenses/history). */
@Schema(description = "Запись истории лицензий")
public record LicenseHistoryResponse(
        Long id,
        Long licenseId,
        LicenseAction action,
        Instant createdAt
) {
    public static LicenseHistoryResponse from(LicenseActivationLog log) {
        return new LicenseHistoryResponse(
                log.getId(),
                log.getLicenseId(),
                log.getAction(),
                log.getCreatedAt()
        );
    }
}
