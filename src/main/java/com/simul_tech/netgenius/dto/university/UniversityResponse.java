package com.simul_tech.netgenius.dto.university;

import com.simul_tech.netgenius.models.University;
import io.swagger.v3.oas.annotations.media.Schema;

/** Публичное представление вуза (GET /universities, /universities/{id}). */
@Schema(description = "Вуз")
public record UniversityResponse(
        @Schema(description = "Идентификатор вуза", example = "1") Long id,
        @Schema(description = "Название", example = "МГУ") String name,
        @Schema(description = "Факультет", example = "ВМК") String faculty,
        @Schema(description = "Специальность", example = "Прикладная математика") String speciality,
        @Schema(description = "Направление", example = "Информатика") String direction
) {
    public static UniversityResponse from(University university) {
        return new UniversityResponse(
                university.getId(),
                university.getName(),
                university.getFaculty(),
                university.getSpeciality(),
                university.getDirection()
        );
    }
}
