package com.simul_tech.netgenius.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.simul_tech.netgenius.validation.RuEmailPolicy;

/** Смена email (PUT /users/email). Требует пароль; новый email требует повторного подтверждения. */
@Schema(description = "Смена email")
public record UpdateEmailRequest(
        @Schema(description = "Новый email: домен должен оканчиваться на .ru (без учёта регистра)", example = "new@mail.ru")
        @NotBlank(message = "Email обязателен") @Email(message = "Некорректный формат email") @Size(max = 255)
        @Pattern(regexp = RuEmailPolicy.REGEXP, message = RuEmailPolicy.MESSAGE)
        String newEmail,
        @Schema(description = "Текущий пароль для подтверждения")
        @NotBlank(message = "Пароль обязателен") String password
) {
}
