package com.simul_tech.netgenius.dto.license;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Активация лицензии текущим пользователем (POST /licenses/activate). */
@Schema(description = "Запрос на активацию лицензии")
public record ActivateLicenseRequest(
        @Schema(description = "Ключ лицензии", example = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890")
        @NotBlank(message = "Ключ лицензии обязателен") String licenseKey
) {
}
