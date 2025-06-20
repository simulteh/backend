package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сущность студента")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор студента", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Полное имя студента", example = "Иванов Иван Иванович")
    private String fullName;

    @Column(nullable = false)
    @Schema(description = "Учебная группа", example = "ИТ-101")
    private String group;

    @Column(nullable = false)
    @Schema(description = "Дата зачисления", example = "2023-09-01")
    private LocalDate enrollmentDate;

    @Column
    @Schema(description = "Статус активности студента", example = "true")
    private Boolean isActive = true;

    @Column
    @Schema(description = "Дата создания записи")
    private LocalDateTime createdAt;

    @Column
    @Schema(description = "Дата последнего обновления записи")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}