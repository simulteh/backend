package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {@UniqueConstraint(columnNames = "email")},
        indexes = {@Index(name = "idx_users_email", columnList = "email")})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    /** Никнейм — не обязан быть уникальным (бизнес-правило). */
    @Column(name = "nickname")
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    /** Роль пользователя. Новый пользователь регистрируется как USER. */
    @Enumerated(EnumType.STRING)
    // DEFAULT также заполняет роль при добавлении колонки в старую непустую таблицу users.
    @Column(name = "role", nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'USER'")
    private Role role = Role.USER;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Признак подтверждённого email (email_confirmed в спецификации). */
    @Column(name = "enabled")
    private boolean enabled;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) {
            role = Role.USER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
