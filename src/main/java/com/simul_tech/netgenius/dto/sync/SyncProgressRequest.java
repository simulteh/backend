package com.simul_tech.netgenius.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Пакет событий прогресса от десктоп-симулятора (POST /sync/progress).
 * Оффлайн-данные старше 14 дней отбрасываются на сервере.
 */
@Schema(description = "Запрос на синхронизацию прогресса")
public record SyncProgressRequest(
        @Schema(description = "Идентификатор устройства/клиента", example = "desktop-win-1.2.0")
        String deviceId,

        @Schema(description = "Накопленные события прогресса")
        @NotNull @Valid List<SyncEventDto> events
) {
}
