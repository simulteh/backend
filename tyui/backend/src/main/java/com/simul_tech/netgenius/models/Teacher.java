package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сущность преподавателя")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор преподавателя", example = "1")
    private Long id;
    @Column(nullable = false)
    @Schema(description = "Имя преподавателя", example = "Иван")
    private String firstName;
    @Column(nullable = false)
    @Schema(description = "Фамилия преподавателя", example = "Петров")
    private String lastName;
    @Column(nullable = false, unique = true)
    @Schema(description = "Email преподавателя (уникальный)", example = "ivan.petrov@university.edu")
    private String email;
    @Column
    @Schema(description = "Номер телефона", example = "+7-999-123-45-67")
    private String phone;
    @Column
    @Schema(description = "Специализация", example = "Математика")
    private String specialization;
    @Column
    @Schema(description = "Отделение/факультет", example = "Факультет математики")
    private String department;
    @Column
    @Schema(description = "Ученая степень", example = "Кандидат наук")
    private String academicDegree;
    @Column
    @Schema(description = "Ученое звание", example = "Доцент")
    private String academicTitle;
    @Column
    @Schema(description = "Опыт работы в годах", example = "10")
    private Integer experienceYears;
    @Column
    @Schema(description = "Статус активности преподавателя", example = "true")
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