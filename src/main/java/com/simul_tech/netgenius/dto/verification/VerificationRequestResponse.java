package com.simul_tech.netgenius.dto.verification;

import com.simul_tech.netgenius.enums.VerificationRequestType;
import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.VerificationRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Заявка на подтверждение роли (ответ пользователю). */
@Schema(description = "Заявка на подтверждение роли")
public record VerificationRequestResponse(
        Long id,
        VerificationRequestType requestType,
        VerificationStatus status,
        String comment,
        Instant createdAt,
        Instant reviewedAt
) {
    public static VerificationRequestResponse from(VerificationRequest r) {
        return new VerificationRequestResponse(
                r.getId(),
                r.getRequestType(),
                r.getStatus(),
                r.getComment(),
                r.getCreatedAt(),
                r.getReviewedAt()
        );
    }
}
