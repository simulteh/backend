package com.simul_tech.netgenius.dto.sync;

import com.simul_tech.netgenius.enums.LabStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Одно событие прогресса, накопленное десктоп-симулятором в оффлайне.
 * Применяется при синхронизации (POST /sync/progress).
 */
@Schema(description = "Событие прогресса по лабораторной работе")
public record SyncEventDto(
        @Schema(description = "Идентификатор лабораторной работы", example = "1")
        @NotNull Long labId,

        @Schema(description = "Статус прохождения")
        LabStatus status,

        @Schema(description = "Время начала попытки")
        Instant startedAt,

        @Schema(description = "Время завершения (если завершена)")
        Instant completedAt,

        @Schema(description = "Оценка за попытку (если есть)", example = "85")
        Integer score,

        @Schema(description = "Версия клиента симулятора (если есть)", example = "1.2.0")
        String clientVersion
) {
}
