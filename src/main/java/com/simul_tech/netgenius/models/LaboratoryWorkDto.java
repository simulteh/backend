package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO для лабораторной работы, используется для возврата данных клиенту.
 */
@Data
@Schema(
    name = "LaboratoryWorkDto",
    description = "DTO для лабораторной работы. " +
                 "Содержит все необходимые поля для отображения информации о работе, " +
                 "исключая служебные поля (id_time, file_path)."
)
public class LaboratoryWorkDto {
    @Schema(
        description = "Уникальный идентификатор работы",
        example = "1",
        required = true
    )
    private Long id_work;

    @Schema(
        description = "Идентификатор студента, выполнившего работу",
        example = "123",
        required = true
    )
    private Long id_user;

    @Schema(
        description = "Идентификатор преподавателя, проверяющего работу",
        example = "456",
        required = true
    )
    private Long id_recipient;

    @Schema(
        description = "Статус работы: completed (выполнена), in_progress (в процессе), graded (оценена)",
        example = "completed",
        required = true
    )
    private String status;

    @Schema(
        description = "Комментарий к работе (максимум 1000 символов)",
        example = "Лабораторная работа по теме 'Сети и телекоммуникации'",
        maxLength = 1000
    )
    private String comment;

    @Schema(
        description = "Оценка за работу (от 0 до 100)",
        example = "85",
        minimum = "0",
        maximum = "100"
    )
    private Integer grade;

    @Schema(
        description = "Флаг, указывающий закрыты ли комментарии к работе",
        example = "false",
        defaultValue = "false"
    )
    private Boolean isClosed;

    @Schema(
        description = "Тип загруженного файла",
        example = "pdf",
        allowableValues = {"pdf", "doc", "docx", "xlsx", "txt", "png", "jpeg", "jpg"}
    )
    private String fileType;
} 