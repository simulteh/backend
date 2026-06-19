package com.simul_tech.netgenius.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** Пара токенов, выдаваемая при логине/обновлении. */
@Schema(description = "Access- и refresh-токены")
public record TokenResponse(
        @Schema(description = "JWT access-токен (15 минут)") String accessToken,
        @Schema(description = "Refresh-токен (30 дней)") String refreshToken,
        @Schema(description = "Тип токена", example = "Bearer") String tokenType,
        @Schema(description = "Время жизни access-токена в мс", example = "900000") long expiresInMs
) {
    public static TokenResponse bearer(String accessToken, String refreshToken, long expiresInMs) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInMs);
    }
}
