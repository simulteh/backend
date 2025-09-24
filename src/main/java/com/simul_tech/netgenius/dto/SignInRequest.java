package com.simul_tech.netgenius.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO для запросов на авторизацию")
public class SignInRequest {

    @Schema(description = "Почта пользователя", example = "ivanov@mail.ru")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(description = "Пароль для входа", example = "ivanov123")
    @NotBlank(message = "Пароль обязателен")
    private String password;
}
