package com.simul_tech.netgenius.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Сводка по результату синхронизации прогресса (ответ POST /sync/progress). */
@Schema(description = "Результат синхронизации прогресса")
public record SyncResultResponse(
        @Schema(description = "Число применённых событий", example = "5")
        int synced,

        @Schema(description = "Число пропущенных событий (устаревшие/некорректные)", example = "2")
        int skipped,

        @Schema(description = "Актуальный прогресс по затронутым лабораторным")
        List<SyncProgressItem> progress
) {
}
