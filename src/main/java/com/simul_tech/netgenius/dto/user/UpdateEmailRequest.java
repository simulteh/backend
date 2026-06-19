package com.simul_tech.netgenius.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Смена email (PUT /users/email). Требует пароль; новый email требует повторного подтверждения. */
@Schema(description = "Смена email")
public record UpdateEmailRequest(
        @Schema(description = "Новый email", example = "new@mail.ru")
        @NotBlank(message = "Email обязателен") @Email(message = "Некорректный формат email") String newEmail,
        @Schema(description = "Текущий пароль для подтверждения")
        @NotBlank(message = "Пароль обязателен") String password
) {
}
