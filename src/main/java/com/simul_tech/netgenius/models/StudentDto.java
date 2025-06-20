package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object для студента")
public class StudentDto {

    @Schema(description = "Уникальный идентификатор студента", example = "1")
    private Long id;

    @Schema(description = "Полное имя студента", example = "Иванов Иван Иванович", required = true)
    private String fullName;

    @Schema(description = "Учебная группа", example = "ИТ-101", required = true)
    private String group;

    @Schema(description = "Дата зачисления (формат: yyyy-MM-dd)", example = "2023-09-01", required = true)
    private LocalDate enrollmentDate;

    @Schema(description = "Статус активности студента", example = "true")
    private Boolean isActive;

    @Schema(description = "Дата создания записи", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}