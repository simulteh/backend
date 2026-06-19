package com.simul_tech.netgenius.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Унифицированный формат тела ответа об ошибке. */
@Schema(description = "Стандартный ответ об ошибке")
public record ApiError(
        @Schema(description = "HTTP-статус", example = "404") int status,
        @Schema(description = "Краткое описание ошибки", example = "Not Found") String error,
        @Schema(description = "Сообщение", example = "Группа не найдена") String message,
        @Schema(description = "Момент возникновения") Instant timestamp
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Instant.now());
    }
}
