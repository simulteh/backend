package com.simul_tech.netgenius.dto.university;

import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.UniversityCreationRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Полный ответ по заявке для администратора (GET /admin/university-requests). */
@Schema(description = "Заявка на создание вуза (для администратора)")
public record UniversityRequestAdminResponse(
        @Schema(description = "Идентификатор заявки", example = "1") Long id,
        @Schema(description = "Идентификатор преподавателя", example = "5") Long teacherId,
        @Schema(description = "Название вуза", example = "МГУ") String universityName,
        @Schema(description = "Факультет", example = "ВМК") String faculty,
        @Schema(description = "Специальность", example = "Прикладная математика") String speciality,
        @Schema(description = "Направление", example = "Информатика") String direction,
        @Schema(description = "Статус", example = "PENDING") VerificationStatus status,
        @Schema(description = "Дата создания") Instant createdAt
) {
    public static UniversityRequestAdminResponse from(UniversityCreationRequest request) {
        return new UniversityRequestAdminResponse(
                request.getId(),
                request.getTeacherId(),
                request.getUniversityName(),
                request.getFaculty(),
                request.getSpeciality(),
                request.getDirection(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
