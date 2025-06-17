package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "laboratory_works")
@Schema(
    name = "LaboratoryWork",
    description = "Сущность лабораторной работы. " +
                 "Содержит все поля, включая служебные (id_time, file_path). " +
                 "Используется для хранения данных в базе данных."
)
public class LaboratoryWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
        description = "Идентификатор отправки (для внутреннего использования)",
        example = "789"
    )
    private String id_departure;

    @Schema(
        description = "Временная метка создания/обновления работы",
        example = "2024-03-20T10:30:00",
        required = true
    )
    private LocalDateTime id_time;

    @Schema(
        description = "Имя загруженного файла",
        example = "lab_work_1.pdf",
        required = true
    )
    private String file_name;

    @Schema(
        description = "Размер файла в байтах",
        example = "1048576",
        required = true
    )
    private Double file_size;

    @Schema(
        description = "Тип загруженного файла",
        example = "pdf",
        allowableValues = {"pdf", "doc", "docx", "xlsx", "txt", "png", "jpeg", "jpg"},
        required = true
    )
    private String file_type;

    @Schema(
        description = "Путь к файлу в системе хранения",
        example = "/uploads/works/2024/03/20/lab_work_1.pdf",
        required = true
    )
    private String file_path;

    @Schema(
        description = "Комментарий к работе (максимум 1000 символов)",
        example = "Лабораторная работа по теме 'Сети и телекоммуникации'",
        maxLength = 1000
    )
    private String comment;

    @Schema(
        description = "Статус работы: completed (выполнена), in_progress (в процессе), graded (оценена)",
        example = "completed",
        required = true
    )
    private String status;

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
    private Boolean is_closed;

    @PrePersist
    protected void onCreate() {
        id_time = LocalDateTime.now();
    }
} 