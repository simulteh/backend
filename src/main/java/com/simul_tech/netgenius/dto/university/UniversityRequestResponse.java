package com.simul_tech.netgenius.dto.university;

import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.UniversityCreationRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Краткий ответ по заявке преподавателя (POST /teacher/university-request). */
@Schema(description = "Заявка на создание вуза (краткая)")
public record UniversityRequestResponse(
        @Schema(description = "Идентификатор заявки", example = "1") Long id,
        @Schema(description = "Статус", example = "PENDING") VerificationStatus status,
        @Schema(description = "Название вуза", example = "МГУ") String universityName,
        @Schema(description = "Дата создания") Instant createdAt
) {
    public static UniversityRequestResponse from(UniversityCreationRequest request) {
        return new UniversityRequestResponse(
                request.getId(),
                request.getStatus(),
                request.getUniversityName(),
                request.getCreatedAt()
        );
    }
}
