package com.simul_tech.netgenius.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Заявка на подтверждение роли студента (POST /verification/student). */
@Schema(description = "Заявка на роль студента")
public record StudentVerificationRequest(
        @Schema(description = "ФИО", example = "Иванов Иван Иванович")
        @NotBlank String fullName,

        @Schema(description = "Год рождения", example = "2003") Integer birthYear,
        @Schema(description = "Телефон", example = "+79991234567") String phone,
        @Schema(description = "ID вуза", example = "1") Long universityId,
        @Schema(description = "Факультет", example = "ФКН") String faculty,
        @Schema(description = "Специальность", example = "Программная инженерия") String speciality,
        @Schema(description = "Направление", example = "09.03.04") String direction,
        @Schema(description = "Курс", example = "2") Integer course,
        @Schema(description = "Степень", example = "Бакалавриат") String degree
) {
}
