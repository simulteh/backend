package com.simul_tech.netgenius.dto.license;

import com.simul_tech.netgenius.models.License;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Лицензия текущего пользователя (GET /licenses/me). Ключ маскируется. */
@Schema(description = "Лицензия пользователя")
public record MyLicenseResponse(
        Long id,
        String licenseKey,
        Instant activatedAt,
        Instant expiresAt,
        boolean valid
) {
    public static MyLicenseResponse from(License license, Instant now) {
        boolean valid = license.isActive()
                && license.getExpiresAt() != null
                && license.getExpiresAt().isAfter(now);
        return new MyLicenseResponse(
                license.getId(),
                mask(license.getLicenseKey()),
                license.getActivatedAt(),
                license.getExpiresAt(),
                valid
        );
    }

    /** Показываем только последние 4 символа ключа. */
    private static String mask(String key) {
        if (key == null || key.length() <= 4) {
            return key;
        }
        return "****-" + key.substring(key.length() - 4);
    }
}
