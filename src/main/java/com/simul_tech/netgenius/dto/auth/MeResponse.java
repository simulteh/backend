package com.simul_tech.netgenius.dto.auth;

import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.models.User;
import io.swagger.v3.oas.annotations.media.Schema;

/** Информация о текущем пользователе (GET /auth/me, GET /users/me). */
@Schema(description = "Текущий пользователь")
public record MeResponse(
        Long id,
        String email,
        String nickname,
        String firstName,
        String lastName,
        String middleName,
        Role role,
        boolean emailConfirmed
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
