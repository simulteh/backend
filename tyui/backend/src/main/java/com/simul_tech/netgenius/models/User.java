package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сущность пользователя системы")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Имя пользователя (уникальное)", example = "john_doe")
    private String username;

    @Column(unique = true, nullable = false)
    @Schema(description = "Email пользователя (уникальный)", example = "john@example.com")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Хешированный пароль пользователя")
    private String password;
    
    @Column
    @Schema(description = "Дата создания аккаунта")
    private LocalDateTime createdAt;
    
    @Column
    @Schema(description = "Дата последнего обновления аккаунта")
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
