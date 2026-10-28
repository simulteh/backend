package com.simul_tech.netgenius.dto.verification;

import com.simul_tech.netgenius.enums.VerificationRequestType;
import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.VerificationRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Заявка на подтверждение роли (ответ администратору, с userId). */
@Schema(description = "Заявка на подтверждение роли (админ)")
public record AdminVerificationResponse(
        @Schema(description = "ID заявки на подтверждение роли. Передавайте это значение в /admin/verifications/{id}/approve или /reject", example = "4")
        Long id,
        @Schema(description = "ID аккаунта пользователя, подавшего заявку. Для approve/reject используется отдельное поле id заявки", example = "26")
        Long userId,
        VerificationRequestType requestType,
        VerificationStatus status,
        String comment,
        Instant createdAt,
        Instant reviewedAt,
        Long reviewedBy
) {
    public static AdminVerificationResponse from(VerificationRequest r) {
        return new AdminVerificationResponse(
                r.getId(),
                r.getUserId(),
                r.getRequestType(),
                r.getStatus(),
                r.getComment(),
                r.getCreatedAt(),
                r.getReviewedAt(),
                r.getReviewedBy()
        );
    }
}
