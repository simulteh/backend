package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание студента")
public class CreateStudentRequest {

    @Schema(description = "Полное имя студента", example = "Иванов Иван Иванович", required = true)
    @NotBlank(message = "ФИО не может быть пустым")
    private String fullName;

    @Schema(description = "Учебная группа", example = "ИТ-101", required = true)
    @NotBlank(message = "Группа не может быть пустой")
    private String group;

    @Schema(description = "Дата зачисления (формат: yyyy-MM-dd)", example = "2023-09-01", required = true)
    @NotNull(message = "Дата зачисления не может быть пустой")
    private LocalDate enrollmentDate;
}