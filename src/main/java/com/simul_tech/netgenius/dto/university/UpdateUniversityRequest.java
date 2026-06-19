package com.simul_tech.netgenius.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;

/** Обновление вуза администратором (PUT /admin/universities/{id}). Все поля необязательны. */
@Schema(description = "Обновление вуза администратором")
public record UpdateUniversityRequest(
        @Schema(description = "Название", example = "МГУ") String name,
        @Schema(description = "Факультет", example = "ВМК") String faculty,
        @Schema(description = "Специальность", example = "Прикладная математика") String speciality,
        @Schema(description = "Направление", example = "Информатика") String direction
) {
}
