package com.simul_tech.netgenius.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

/** Простой ответ с текстовым сообщением. */
@Schema(description = "Ответ с сообщением")
public record MessageResponse(
        @Schema(description = "Сообщение", example = "Операция выполнена") String message
) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
