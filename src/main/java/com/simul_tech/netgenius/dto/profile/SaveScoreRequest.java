package com.simul_tech.netgenius.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaveScoreRequest(
        @Schema(description = "Уровень: 0 — обучение, 1–10 — игровые уровни", minimum = "0", maximum = "10")
        @NotNull @Min(0) @Max(10) Integer level,
        @Schema(description = "Балл; сохраняется лучший результат", minimum = "0", maximum = "10")
        @NotNull @Min(0) @Max(10) Integer score
) {
}
