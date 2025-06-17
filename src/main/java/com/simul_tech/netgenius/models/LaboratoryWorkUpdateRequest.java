package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
    name = "LaboratoryWorkUpdateRequest",
    description = "DTO для обновления данных лабораторной работы. " +
                 "Содержит только те поля, которые могут быть изменены: " +
                 "статус, оценка и флаг закрытия комментариев."
)
public class LaboratoryWorkUpdateRequest {
    @Schema(
        description = "Новый статус работы: completed (выполнена), in_progress (в процессе), graded (оценена)",
        example = "completed",
        allowableValues = {"completed", "in_progress", "graded"}
    )
    private String status;

    @Schema(
        description = "Новая оценка за работу (от 0 до 100)",
        example = "85",
        minimum = "0",
        maximum = "100"
    )
    private Integer grade;

    @Schema(
        description = "Новый флаг закрытия комментариев",
        example = "true",
        defaultValue = "false"
    )
    private Boolean isClosed;
} 