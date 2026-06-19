package com.simul_tech.netgenius.dto.sync;

import com.simul_tech.netgenius.enums.LabStatus;
import com.simul_tech.netgenius.models.StudentLabProgress;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Актуальный прогресс по одной затронутой синхронизацией лабораторной работе. */
@Schema(description = "Прогресс по лабораторной работе после синхронизации")
public record SyncProgressItem(
        Long labId,
        LabStatus status,
        Integer bestScore,
        int attemptsCount,
        Instant lastCompletedAt
) {
    public static SyncProgressItem from(StudentLabProgress progress) {
        return new SyncProgressItem(
                progress.getLabId(),
                progress.getStatus(),
                progress.getBestScore(),
                progress.getAttemptsCount(),
                progress.getLastCompletedAt()
        );
    }
}
