package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Универсальный ответ API")
public class ApiResponse<T> {
    
    @Schema(description = "Статус операции", example = "success")
    private String status;
    
    @Schema(description = "Сообщение", example = "Операция выполнена успешно")
    private String message;
    
    @Schema(description = "Данные ответа")
    private T data;
    
    @Schema(description = "Временная метка", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "Операция выполнена успешно", data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String status, String message) {
        return new ApiResponse<>(status, message, null, LocalDateTime.now());
    }
} 