package com.simul_tech.netgenius.entity; // Убедись, что пакет правильный

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // Если планируешь хранить дату сдачи оценки

@Entity
@Table(name = "grades") // Имя таблицы в базе данных (можно выбрать другое)
@Data // Lombok: генерирует геттеры, сеттеры, equals, hashCode, toString
@NoArgsConstructor // Lombok: генерирует конструктор без аргументов
@AllArgsConstructor // Lombok: генерирует конструктор со всеми аргументами
public class Grade {

    @Id // Указывает, что это первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Уникальный идентификатор оценки

    private Integer score; // Сама оценка (например, число от 1 до 100)
    private String feedback; // Отзыв или комментарии к оценке
    private LocalDate submissionDate; // Дата получения оценки

    // TODO: Если Grade связан с другими сущностями (например, со студентом и курсом),
    // добавь их здесь. Например:

}