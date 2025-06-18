package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание преподавателя")
public class CreateTeacherRequest {
    
    @Schema(description = "Имя преподавателя", example = "Иван", required = true)
    private String firstName;
    
    @Schema(description = "Фамилия преподавателя", example = "Петров", required = true)
    private String lastName;
    
    @Schema(description = "Email преподавателя (уникальный)", example = "ivan.petrov@university.edu", required = true)
    private String email;
    
    @Schema(description = "Номер телефона", example = "+7-999-123-45-67")
    private String phone;
    
    @Schema(description = "Специализация", example = "Математика")
    private String specialization;
    
    @Schema(description = "Отделение/факультет", example = "Факультет математики")
    private String department;
    
    @Schema(description = "Ученая степень", example = "Кандидат наук")
    private String academicDegree;
    
    @Schema(description = "Ученое звание", example = "Доцент")
    private String academicTitle;
    
    @Schema(description = "Опыт работы в годах", example = "10")
    private Integer experienceYears;
} 