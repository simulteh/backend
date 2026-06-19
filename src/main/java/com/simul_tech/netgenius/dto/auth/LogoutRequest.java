package com.simul_tech.netgenius.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на выход (отзыв refresh-токенов)")
public record LogoutRequest(
        @Schema(description = "Refresh-токен сессии") @NotBlank(message = "refreshToken обязателен") String refreshToken
) {
}
