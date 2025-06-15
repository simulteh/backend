package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "laboratory_works")
@Data
@Schema(description = "Лабораторная работа")
public class LaboratoryWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор работы")
    private Long id_work;

    @Column(name = "id_user")
    @Schema(description = "Идентификатор отправителя (студента)")
    private Long id_user;

    @Column(name = "id_recipient")
    @Schema(description = "Идентификатор получателя (преподавателя)")
    private Long id_recipient;

    @Column(name = "id_departure")
    @Schema(description = "Идентификатор отправленного сообщения")
    private String id_departure;

    @Column(name = "id_time")
    @Schema(description = "Дата и время отправки")
    private LocalDateTime id_time;

    @Column(name = "file_name")
    @Schema(description = "Название файла")
    private String file_name;

    @Column(name = "file_path")
    @Schema(description = "Путь к файлу в хранилище")
    private String file_path;

    @Column(name = "file_size")
    @Schema(description = "Размер файла в МБ")
    private Double file_size;

    @Column(name = "file_type")
    @Schema(description = "Тип файла (pdf, doc, xlsx, txt, png, jpeg)")
    private String file_type;

    @Column(name = "comment", length = 1000)
    @Schema(description = "Комментарий студента (макс. 1000 символов)")
    private String comment;

    @Column(name = "status")
    @Schema(description = "Статус работы (completed, in_progress, graded)")
    private String status;

    @Column(name = "grade")
    @Schema(description = "Оценка за работу")
    private Integer grade;

    @Column(name = "is_closed")
    @Schema(description = "Флаг закрытия комментариев")
    private Boolean is_closed = false;

    @PrePersist
    protected void onCreate() {
        id_time = LocalDateTime.now();
    }
} 