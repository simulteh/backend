package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с ошибкой API")
public class ErrorResponse {
    
    @Schema(description = "HTTP статус код", example = "400")
    private int status;
    
    @Schema(description = "Тип ошибки", example = "BAD_REQUEST")
    private String error;
    
    @Schema(description = "Сообщение об ошибке", example = "Некорректные данные запроса")
    private String message;
    
    @Schema(description = "Путь запроса", example = "/api/teachers")
    private String path;
    
    @Schema(description = "Временная метка", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
    
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
} 