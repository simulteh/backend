package com.simul_tech.netgenius.dto.license;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

/** Создание партии лицензий администратором (POST /admin/licenses/batch). */
@Schema(description = "Запрос на создание партии лицензий")
public record CreateLicenseBatchRequest(
        @Schema(description = "Идентификатор вуза (опционально)", example = "1")
        Long universityId,
        @Schema(description = "Количество лицензий", example = "50")
        @Positive(message = "Количество должно быть положительным") int quantity
) {
}
