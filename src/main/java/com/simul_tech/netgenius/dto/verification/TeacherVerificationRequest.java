package com.simul_tech.netgenius.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Заявка на подтверждение роли преподавателя (POST /verification/teacher). */
@Schema(description = "Заявка на роль преподавателя")
public record TeacherVerificationRequest(
        @Schema(description = "ФИО", example = "Петров Пётр Петрович")
        @NotBlank String fullName,

        @Schema(description = "Телефон", example = "+79991234567") String phone,
        @Schema(description = "Место работы", example = "ВГУ") String workplace,
        @Schema(description = "Должность", example = "Доцент") String position,

        @Schema(description = "URL подтверждающего документа") String documentUrl,
        @Schema(description = "Тип документа", example = "EMPLOYMENT_PROOF") String documentType
) {
}
