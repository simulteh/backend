package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию нового пользователя")
public class SignUpRequest {
    
    @Schema(description = "Имя пользователя (уникальное)", example = "john_doe", required = true)
    private String username;
    
    @Schema(description = "Email пользователя (уникальный)", example = "john@example.com", required = true)
    private String email;
    
    @Schema(description = "Пароль пользователя", example = "password123", required = true)
    private String password;
} 