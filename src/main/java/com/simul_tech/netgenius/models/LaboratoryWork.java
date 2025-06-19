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
                 "Содержит все поля, включая служебные (idTime, filePath). " +
                 "Используется для хранения данных в базе данных."
)
public class LaboratoryWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор работы", example = "1", required = true)
    @Column(name = "id_work")
    private Long idWork;

    @Schema(description = "Идентификатор студента, выполнившего работу", example = "123", required = true)
    @Column(name = "id_user")
    private Long idUser;

    @Schema(description = "Идентификатор преподавателя, проверяющего работу", example = "456", required = true)
    @Column(name = "id_recipient")
    private Long idRecipient;

    @Schema(description = "Идентификатор отправки (для внутреннего использования)", example = "789")
    @Column(name = "id_departure")
    private String idDeparture;

    @Schema(description = "Временная метка создания/обновления работы", example = "2024-03-20T10:30:00", required = true)
    @Column(name = "id_time")
    private LocalDateTime idTime;

    @Schema(description = "Имя загруженного файла", example = "lab_work_1.pdf", required = true)
    @Column(name = "file_name")
    private String fileName;

    @Schema(description = "Размер файла в МБ", example = "1.5", required = true)
    @Column(name = "file_size")
    private Double fileSize;

    @Schema(description = "Тип загруженного файла", example = "pdf", allowableValues = {"pdf", "doc", "docx", "xlsx", "txt", "png", "jpeg", "jpg"}, required = true)
    @Column(name = "file_type")
    private String fileType;

    @Schema(description = "Путь к файлу в системе хранения", example = "/uploads/works/2024/03/20/lab_work_1.pdf", required = true)
    @Column(name = "file_path")
    private String filePath;

    @Schema(description = "Комментарий к работе (максимум 1000 символов)", example = "Лабораторная работа по теме 'Сети и телекоммуникации'", maxLength = 1000)
    @Column(name = "comment", length = 1000)
    private String comment;

    @Schema(description = "Статус работы: completed (выполнена), in_progress (в процессе), graded (оценена)", example = "completed", required = true)
    @Column(name = "status")
    private String status;

    @Schema(description = "Оценка за работу (от 0 до 100)", example = "85", minimum = "0", maximum = "100")
    @Column(name = "grade")
    private Integer grade;

    @Schema(description = "Флаг, указывающий закрыты ли комментарии к работе", example = "false", defaultValue = "false")
    @Column(name = "is_closed")
    private Boolean isClosed = false;

    @PrePersist
    protected void onCreate() {
        idTime = LocalDateTime.now();
    }
} 