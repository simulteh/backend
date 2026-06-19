package com.simul_tech.netgenius.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

/** Обновление профиля пользователя (PUT /users/profile). Все поля необязательны. */
@Schema(description = "Обновление профиля")
public record UpdateProfileRequest(
        @Schema(description = "Имя", example = "Иван") String firstName,
        @Schema(description = "Фамилия", example = "Иванов") String lastName,
        @Schema(description = "Отчество", example = "Иванович") String middleName,
        @Schema(description = "Никнейм", example = "ivan") String nickname
) {
}
