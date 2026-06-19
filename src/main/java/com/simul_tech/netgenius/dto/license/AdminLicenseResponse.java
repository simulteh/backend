package com.simul_tech.netgenius.dto.license;

import com.simul_tech.netgenius.models.License;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Полное представление лицензии для администратора (GET /admin/licenses). */
@Schema(description = "Лицензия (админ)")
public record AdminLicenseResponse(
        Long id,
        String licenseKey,
        Long ownerUserId,
        Instant activatedAt,
        Instant expiresAt,
        boolean active,
        Long batchId
) {
    public static AdminLicenseResponse from(License license) {
        return new AdminLicenseResponse(
                license.getId(),
                license.getLicenseKey(),
                license.getOwnerUserId(),
                license.getActivatedAt(),
                license.getExpiresAt(),
                license.isActive(),
                license.getBatchId()
        );
    }
}
