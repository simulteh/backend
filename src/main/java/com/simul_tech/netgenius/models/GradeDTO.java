package com.simul_tech.netgenius.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // Если используешь LocalDate в Grade

@Data // Lombok: генерирует геттеры, сеттеры, equals, hashCode, toString
@NoArgsConstructor // Lombok: генерирует конструктор без аргументов
@AllArgsConstructor // Lombok: генерирует конструктор со всеми аргументами
public class GradeDTO {
    private Long id; // ID может быть null, когда создаем новую оценку
    private Integer score;
    private String feedback;
    private LocalDate submissionDate;

}