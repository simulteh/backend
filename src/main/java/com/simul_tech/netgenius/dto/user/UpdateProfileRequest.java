package com.simul_tech.netgenius.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** Обновление профиля пользователя (PUT /users/profile). Все поля необязательны. */
@Schema(description = "Обновление профиля")
public record UpdateProfileRequest(
        @Schema(description = "Имя", example = "Иван") @Size(max = 255) String firstName,
        @Schema(description = "Фамилия", example = "Иванов") @Size(max = 255) String lastName,
        @Schema(description = "Отчество", example = "Иванович") @Size(max = 255) String middleName,
        @Schema(description = "Никнейм", example = "ivan") @Size(max = 255) String nickname
) {
}
