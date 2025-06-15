package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Данные для обновления лабораторной работы")
public class LaboratoryWorkUpdateRequest {
    @Schema(description = "Статус работы (completed, in_progress, graded)")
    private String status;

    @Schema(description = "Оценка за работу")
    private Integer grade;

    @Schema(description = "Флаг закрытия комментариев")
    private Boolean isClosed;
} 