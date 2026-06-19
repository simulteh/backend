package com.simul_tech.netgenius.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на смену пароля")
public record ChangePasswordRequest(
        @Schema(description = "Текущий пароль") @NotBlank(message = "Текущий пароль обязателен") String oldPassword,
        @Schema(description = "Новый пароль") @NotBlank(message = "Новый пароль обязателен")
        @Size(min = 6, message = "Пароль должен быть больше 6 символов") String newPassword
) {
}
