package com.simul_tech.netgenius.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса на регистрацию")
public class SignUpRequest {
    @Schema(description = "Имя пользователя", example = "Иван")
    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    private String middleName;

    @Schema(description = "Почта пользователя", example = "ivanov@mail.ru")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(description = "Пароль для входа", example = "ivanov123")
    @NotBlank(message = "Пароль обязателен")
    private String password;
}
