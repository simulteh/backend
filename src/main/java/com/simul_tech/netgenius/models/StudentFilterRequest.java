package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для фильтрации студентов")
public class StudentFilterRequest {

    @Schema(description = "Фильтр по ID студента (точное совпадение)", example = "1")
    private Long id;

    @Schema(description = "Фильтр по ФИО (частичное совпадение без учета регистра)", example = "Иванов")
    private String fullName;

    @Schema(description = "Фильтр по группе (точное или частичное совпадение)", example = "ИТ-101")
    private String group;

    @Schema(description = "Начальная дата зачисления (фильтр 'после')", example = "2023-09-01")
    private String startEnrollmentDate;

    @Schema(description = "Конечная дата зачисления (фильтр 'до')", example = "2023-09-30")
    private String endEnrollmentDate;

    @Schema(description = "Статус активности студента", example = "true")
    private Boolean isActive;

    @Schema(description = "Поле для сортировки",
            example = "fullName",
            allowableValues = {"id", "fullName", "group", "enrollmentDate"},
            defaultValue = "id")
    private String sortBy = "id";

    @Schema(description = "Направление сортировки",
            example = "ASC",
            allowableValues = {"ASC", "DESC"},
            defaultValue = "ASC")
    private String sortDirection = "ASC";

    @Schema(description = "Номер страницы (начиная с 0)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Schema(description = "Размер страницы", example = "10", defaultValue = "10")
    private Integer size = 10;
}