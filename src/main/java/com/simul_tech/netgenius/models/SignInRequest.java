package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на авторизацию в системе управления студентами")
public class SignInRequest {

    @Schema(
            description = "Уникальный идентификатор пользователя (email или логин)",
            example = "admin@university.edu",
            required = true
    )
    @NotBlank(message = "Идентификатор пользователя не может быть пустым")
    private String username;

    @Schema(
            description = "Пароль для доступа к системе",
            example = "securePassword123",
            required = true
    )
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
