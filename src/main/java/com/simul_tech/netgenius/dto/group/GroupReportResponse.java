package com.simul_tech.netgenius.dto.group;

import com.simul_tech.netgenius.enums.LabStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/** Отчёт по группе: матрица "студенты x лабораторные" с лучшими оценками. */
@Schema(description = "Отчёт по успеваемости группы")
public record GroupReportResponse(
        Long groupId,
        List<LabColumn> labs,
        List<StudentRow> rows
) {
    /** Колонка отчёта — лабораторная работа. */
    @Schema(description = "Лабораторная работа (колонка отчёта)")
    public record LabColumn(
            Long labId,
            String title
    ) {
    }

    /** Строка отчёта — студент и его результаты по всем лабораторным. */
    @Schema(description = "Студент и его результаты (строка отчёта)")
    public record StudentRow(
            Long studentId,
            String fullName,
            List<Cell> cells
    ) {
    }

    /** Ячейка отчёта — результат студента по конкретной лабораторной. */
    @Schema(description = "Результат студента по лабораторной (ячейка отчёта)")
    public record Cell(
            Long labId,
            Integer bestScore,
            LabStatus status,
            Instant lastCompletedAt
    ) {
    }
}
