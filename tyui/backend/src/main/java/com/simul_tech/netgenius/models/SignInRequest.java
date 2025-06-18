package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на авторизацию пользователя")
public class SignInRequest {
    
    @Schema(description = "Имя пользователя", example = "john_doe", required = true)
    private String username;
    
    @Schema(description = "Пароль пользователя", example = "password123", required = true)
    private String password;
} 