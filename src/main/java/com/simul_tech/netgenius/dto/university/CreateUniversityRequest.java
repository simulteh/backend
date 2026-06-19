package com.simul_tech.netgenius.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Создание вуза администратором (POST /admin/universities). */
@Schema(description = "Создание вуза администратором")
public record CreateUniversityRequest(
        @Schema(description = "Название", example = "МГУ")
        @NotBlank(message = "Название обязательно") String name,
        @Schema(description = "Факультет", example = "ВМК") String faculty,
        @Schema(description = "Специальность", example = "Прикладная математика") String speciality,
        @Schema(description = "Направление", example = "Информатика") String direction
) {
}
