package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Стандартный формат ошибки API для модуля студентов")
public class ErrorResponse {

    @Schema(
            description = "HTTP статус код ошибки",
            example = "400",
            required = true
    )
    private int status;

    @Schema(
            description = "Тип ошибки (стандартный код)",
            example = "STUDENT_VALIDATION_ERROR",
            required = true
    )
    private String errorCode;

    @Schema(
            description = "Человекочитаемое сообщение об ошибке",
            example = "ФИО студента не может быть пустым",
            required = true
    )
    private String message;

    @Schema(
            description = "Дополнительные детали ошибки (опционально)",
            example = "Поле fullName должно содержать минимум 2 символа"
    )
    private String details;

    @Schema(
            description = "Временная метка возникновения ошибки",
            example = "2024-01-15T10:30:00",
            required = true
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "Путь API, на котором произошла ошибка",
            example = "/api/students",
            required = true
    )
    private String path;

    public ErrorResponse(int status, String errorCode, String message, String path) {
        this(status, errorCode, message, null, LocalDateTime.now(), path);
    }

    public ErrorResponse(int status, String errorCode, String message, String details, String path) {
        this(status, errorCode, message, details, LocalDateTime.now(), path);
    }

    // Статические фабричные методы для стандартных ошибок

    public static ErrorResponse validationError(String message, String details, String path) {
        return new ErrorResponse(
                400,
                "STUDENT_VALIDATION_ERROR",
                message,
                details,
                path
        );
    }

    public static ErrorResponse notFound(String entityName, String path) {
        return new ErrorResponse(
                404,
                "STUDENT_NOT_FOUND",
                entityName + " не найден",
                path
        );
    }

    public static ErrorResponse conflict(String message, String path) {
        return new ErrorResponse(
                409,
                "STUDENT_CONFLICT",
                message,
                path
        );
    }
}