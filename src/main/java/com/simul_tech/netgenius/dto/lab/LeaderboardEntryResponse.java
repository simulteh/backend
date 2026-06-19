package com.simul_tech.netgenius.dto.lab;

import io.swagger.v3.oas.annotations.media.Schema;

/** Строка лидерборда лабораторной работы. */
@Schema(description = "Строка лидерборда")
public record LeaderboardEntryResponse(
        int rank,
        Long studentId,
        String studentName,
        Integer bestScore
) {
}
