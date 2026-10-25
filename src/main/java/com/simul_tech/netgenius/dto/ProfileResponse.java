package com.simul_tech.netgenius.dto;

import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса-ответа для личного кабинета")
@AllArgsConstructor
public class ProfileResponse {
    @Schema(description = "ID пользователя; передаётся как studentId при добавлении в группу", example = "42")
    private Long id;

    @Schema(description = "Текущая роль пользователя", example = "STUDENT")
    private Role role;

    @Schema(description = "URL аватара, null если фото отсутствует", nullable = true)
    private String avatarUrl;

    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    private String middleName;

    @Schema(description = "Почта пользователя", example = "ivanov@mail.ru")
    private String email;

    public static ProfileResponse from(User user) {
        return new ProfileResponse(user.getId(), user.getRole(), null, user.getFirstName(),
                user.getLastName(), user.getMiddleName(), user.getEmail());
    }
}
