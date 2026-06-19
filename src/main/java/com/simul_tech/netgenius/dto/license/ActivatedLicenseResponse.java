package com.simul_tech.netgenius.dto.license;

import com.simul_tech.netgenius.models.License;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Результат активации лицензии (POST /licenses/activate). */
@Schema(description = "Активированная лицензия")
public record ActivatedLicenseResponse(
        Long id,
        Instant expiresAt,
        boolean active
) {
    public static ActivatedLicenseResponse from(License license) {
        return new ActivatedLicenseResponse(
                license.getId(),
                license.getExpiresAt(),
                license.isActive()
        );
    }
}
