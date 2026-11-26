package com.simul_tech.netgenius.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.simul_tech.netgenius.validation.RuEmailPolicy;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса на регистрацию")
public class SignUpRequest {
    @Schema(description = "Имя пользователя", example = "Иван")
    @NotBlank(message = "Имя обязательно")
    @Size(max = 255)
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 255)
    private String lastName;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    @Size(max = 255)
    private String middleName;

    @Schema(description = "Никнейм (не обязан быть уникальным)", example = "ivan")
    @Size(max = 255)
    private String nickname;

    @Schema(description = "Почта пользователя: домен должен оканчиваться на .ru (без учёта регистра)", example = "ivanov@mail.ru")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Pattern(regexp = RuEmailPolicy.REGEXP, message = RuEmailPolicy.MESSAGE)
    @Size(max = 255)
    private String email;

    @Schema(description = "Пароль для входа", example = "ivanov123")
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть больше 6 символов")
    private String password;
}
