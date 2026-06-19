package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.ProfileResponse;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.services.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Устаревший эндпоинт личного кабинета. Контракт спецификации — GET /users/me
 * (см. {@link UserController}); этот путь сохранён для обратной совместимости и
 * резолвит пользователя через SecurityContext (без повторного разбора JWT).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Tag(name = "Profile (legacy)", description = "Устаревший личный кабинет; используйте /users/me")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    @Operation(summary = "Данные текущего пользователя (устаревший путь, см. /users/me)")
    public ResponseEntity<ProfileResponse> getInfo() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(new ProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getEmail()
        ));
    }
}
