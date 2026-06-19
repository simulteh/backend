package com.simul_tech.netgenius.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

/**
 * Приглашение студента в группу (POST /teacher/groups/{id}/invite).
 * Должно быть задано хотя бы одно из полей: email или studentId.
 */
@Schema(description = "Запрос на приглашение студента в группу")
public record InviteRequest(
        @Schema(description = "Email приглашаемого", example = "student@example.com")
        @Email(message = "Некорректный email")
        String email,

        @Schema(description = "Идентификатор студента", example = "5")
        Long studentId
) {
}
