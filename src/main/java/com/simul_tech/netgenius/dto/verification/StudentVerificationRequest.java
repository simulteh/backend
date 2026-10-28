package com.simul_tech.netgenius.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Year;
import java.time.ZoneOffset;

/** Заявка на подтверждение роли студента (POST /verification/student). */
@Schema(description = "Заявка на роль студента")
public record StudentVerificationRequest(
        @Schema(description = "ФИО", example = "Иванов Иван Иванович")
        @NotBlank @Size(max = 255) String fullName,

        @Schema(description = "Год рождения", example = "2003") Integer birthYear,
        @Schema(description = "Телефон", example = "+79991234567") @Size(max = 255) String phone,
        @Schema(description = "ID вуза", example = "1") @Positive Long universityId,
        @Schema(description = "Факультет", example = "ФКН") @Size(max = 255) String faculty,
        @Schema(description = "Специальность", example = "Программная инженерия") @Size(max = 255) String speciality,
        @Schema(description = "Направление", example = "09.03.04") @Size(max = 255) String direction,
        @Schema(description = "Курс", example = "2") @Positive Integer course,
        @Schema(description = "Степень", example = "Бакалавриат") @Size(max = 255) String degree
) {
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "Год рождения должен быть от 1900 до текущего года")
    public boolean isBirthYearValid() {
        return birthYear == null || (birthYear >= 1900 && birthYear <= Year.now(ZoneOffset.UTC).getValue());
    }
}
