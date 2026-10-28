package com.simul_tech.netgenius.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Заявка на подтверждение роли преподавателя (POST /verification/teacher). */
@Schema(description = "Заявка на роль преподавателя")
public record TeacherVerificationRequest(
        @Schema(description = "ФИО", example = "Петров Пётр Петрович")
        @NotBlank @Size(max = 255) String fullName,

        @Schema(description = "Телефон", example = "+79991234567") @Size(max = 255) String phone,
        @Schema(description = "Место работы", example = "ВГУ") @Size(max = 255) String workplace,
        @Schema(description = "Должность", example = "Доцент") @Size(max = 255) String position
) {
}
