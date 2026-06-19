package com.simul_tech.netgenius.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;

/** Тело запроса на отклонение заявки (POST /admin/verifications/{id}/reject). Комментарий необязателен. */
@Schema(description = "Отклонение заявки")
public record RejectVerificationRequest(
        @Schema(description = "Причина отклонения") String comment
) {
}
