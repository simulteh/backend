package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Стандартизированный ответ API для модуля управления студентами
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Стандартный ответ API модуля управления студентами")
public class ApiResponse<T> {

    @Schema(
            description = "Статус выполнения операции: success/error",
            example = "success",
            allowableValues = {"success", "error"}
    )
    private String status;

    @Schema(
            description = "Детальное сообщение о результате операции",
            example = "Данные студента успешно получены"
    )
    private String message;

    @Schema(
            description = "Основные данные ответа (может быть null в случае ошибки)",
            example = "{\"id\": 1, \"fullName\": \"Иванов Иван\", \"group\": \"ИТ-101\", \"enrollmentDate\": \"2023-09-01\"}"
    )
    private T data;

    @Schema(
            description = "Временная метка выполнения операции в формате ISO-8601",
            example = "2024-01-15T10:30:00"
    )
    private LocalDateTime timestamp;

    // Успешные ответы

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                "success",
                "Операция выполнена успешно",
                data,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                "success",
                message,
                data,
                LocalDateTime.now()
        );
    }

    // Ответы с ошибками

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                "error",
                message,
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> notFound(String entityName) {
        return new ApiResponse<>(
                "error",
                String.format("%s не найден", entityName),
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> validationError(String details) {
        return new ApiResponse<>(
                "error",
                "Ошибка валидации данных",
                details,
                LocalDateTime.now()
        );
    }

    /**
     * Создает успешный ответ для операции создания студента
     */
    public static ApiResponse<StudentDto> studentCreated(StudentDto student) {
        return new ApiResponse<>(
                "success",
                "Студент успешно зарегистрирован",
                student,
                LocalDateTime.now()
        );
    }

    /**
     * Создает успешный ответ для операции получения данных студента
     */
    public static ApiResponse<StudentDto> studentFound(StudentDto student) {
        return new ApiResponse<>(
                "success",
                "Данные студента успешно получены",
                student,
                LocalDateTime.now()
        );
    }
}