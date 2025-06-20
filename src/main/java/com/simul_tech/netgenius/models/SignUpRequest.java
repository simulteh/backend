package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию администратора системы управления студентами")
public class SignUpRequest {

    @Schema(
            description = "Уникальный логин администратора (только латинские буквы и цифры)",
            example = "admin_university",
            required = true
    )
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 4, max = 20, message = "Логин должен быть от 4 до 20 символов")
    private String username;

    @Schema(
            description = "Корпоративный email университета",
            example = "admin@university.edu",
            required = true
    )
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(
            description = "Пароль (минимум 8 символов, включая цифры и буквы)",
            example = "SecurePass123",
            required = true
    )
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    private String password;
}