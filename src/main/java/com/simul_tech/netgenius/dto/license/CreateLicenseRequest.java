package com.simul_tech.netgenius.dto.license;

import io.swagger.v3.oas.annotations.media.Schema;

/** Создание одиночной лицензии администратором (POST /admin/licenses/create). Тело опционально. */
@Schema(description = "Запрос на создание лицензии")
public record CreateLicenseRequest(
        @Schema(description = "Идентификатор вуза (опционально)", example = "1")
        Long universityId
) {
}
