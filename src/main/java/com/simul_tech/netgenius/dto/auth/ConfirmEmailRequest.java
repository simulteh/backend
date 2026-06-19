package com.simul_tech.netgenius.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на подтверждение email")
public record ConfirmEmailRequest(
        @Schema(description = "Токен подтверждения из письма") @NotBlank(message = "Токен обязателен") String token
) {
}
