package com.simul_tech.netgenius.dto.sync;

import com.simul_tech.netgenius.enums.LabStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Одно событие прогресса, накопленное десктоп-симулятором в оффлайне.
 * Применяется при синхронизации (POST /sync/progress).
 */
@Schema(description = "Событие прогресса по лабораторной работе")
public record SyncEventDto(
        @Schema(description = "Идентификатор лабораторной работы", example = "1")
        @NotNull @Positive Long labId,

        @Schema(description = "Статус прохождения")
        @NotNull LabStatus status,

        @Schema(description = "Время начала попытки")
        Instant startedAt,

        @Schema(description = "Время завершения (если завершена)")
        Instant completedAt,

        @Schema(description = "Оценка LMS 0–100 (отдельно от оценок /profile/scores 0–10)", example = "85")
        @Min(0) @Max(100) Integer score,

        @Schema(description = "Версия клиента симулятора (если есть)", example = "1.2.0")
        @Size(max = 255) String clientVersion,

        @Schema(description = "Постоянный ID события, например UUID. Повторная отправка не создаёт новую попытку")
        @Size(min = 1, max = 100) String eventId
) {
}
