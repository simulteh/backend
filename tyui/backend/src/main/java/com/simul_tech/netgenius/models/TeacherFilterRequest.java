package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для фильтрации преподавателей")
public class TeacherFilterRequest {
    
    @Schema(description = "Фильтр по имени (частичное совпадение)", example = "Иван")
    private String firstName;
    
    @Schema(description = "Фильтр по фамилии (частичное совпадение)", example = "Петров")
    private String lastName;
    
    @Schema(description = "Фильтр по email (частичное совпадение)", example = "ivan.petrov")
    private String email;
    
    @Schema(description = "Фильтр по специализации (частичное совпадение)", example = "Математика")
    private String specialization;
    
    @Schema(description = "Фильтр по отделению (частичное совпадение)", example = "Факультет математики")
    private String department;
    
    @Schema(description = "Фильтр по ученой степени (точное совпадение)", example = "Кандидат наук")
    private String academicDegree;
    
    @Schema(description = "Фильтр по ученому званию (точное совпадение)", example = "Доцент")
    private String academicTitle;
    
    @Schema(description = "Минимальный опыт работы в годах", example = "5")
    private Integer minExperienceYears;
    
    @Schema(description = "Максимальный опыт работы в годах", example = "15")
    private Integer maxExperienceYears;
    
    @Schema(description = "Фильтр по статусу активности", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Поле для сортировки", example = "lastName", defaultValue = "lastName")
    private String sortBy = "lastName";
    
    @Schema(description = "Направление сортировки", example = "ASC", allowableValues = {"ASC", "DESC"}, defaultValue = "ASC")
    private String sortDirection = "ASC";
    
    @Schema(description = "Номер страницы (начиная с 0)", example = "0", defaultValue = "0")
    private Integer page = 0;
    
    @Schema(description = "Размер страницы", example = "10", defaultValue = "10")
    private Integer size = 10;
} 