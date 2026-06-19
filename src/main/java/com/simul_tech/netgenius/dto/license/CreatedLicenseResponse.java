package com.simul_tech.netgenius.dto.license;

import com.simul_tech.netgenius.models.License;
import io.swagger.v3.oas.annotations.media.Schema;

/** Созданная администратором лицензия (POST /admin/licenses/create). */
@Schema(description = "Созданная лицензия")
public record CreatedLicenseResponse(
        Long id,
        String licenseKey
) {
    public static CreatedLicenseResponse from(License license) {
        return new CreatedLicenseResponse(license.getId(), license.getLicenseKey());
    }
}
