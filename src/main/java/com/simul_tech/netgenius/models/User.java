package com.simul_tech.netgenius.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сущность пользователя системы управления студентами")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @Schema(description = "Уникальное имя пользователя (только латинские буквы и цифры)",
            example = "admin_user",
            required = true)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    @Schema(description = "Корпоративный email университета",
            example = "admin@university.edu",
            required = true)
    private String email;

    @Column(nullable = false)
    @Schema(description = "Хешированный пароль пользователя (BCrypt)",
            required = true)
    private String password;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Schema(description = "Статус активности аккаунта", example = "true")
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Дата создания аккаунта", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Schema(description = "Дата последнего обновления аккаунта", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Column
    @Schema(description = "Роль пользователя в системе", example = "ADMIN")
    private String role = "ADMIN";

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}