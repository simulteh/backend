package com.simul_tech.netgenius.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Заявка преподавателя на создание вуза (POST /teacher/university-request). */
@Schema(description = "Заявка на создание вуза")
public record UniversityRequestCreateRequest(
        @Schema(description = "Название вуза", example = "МГУ")
        @NotBlank(message = "Название вуза обязательно") String universityName,
        @Schema(description = "Факультет", example = "ВМК") String faculty,
        @Schema(description = "Специальность", example = "Прикладная математика") String speciality,
        @Schema(description = "Направление", example = "Информатика") String direction
) {
}
