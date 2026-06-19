package com.simul_tech.netgenius.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на обновление токена")
public record RefreshRequest(
        @Schema(description = "Refresh-токен") @NotBlank(message = "refreshToken обязателен") String refreshToken
) {
}
