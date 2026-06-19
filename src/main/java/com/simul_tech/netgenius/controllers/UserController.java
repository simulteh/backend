package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.auth.MeResponse;
import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.user.UpdateEmailRequest;
import com.simul_tech.netgenius.dto.user.UpdateProfileRequest;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.services.CurrentUserService;
import com.simul_tech.netgenius.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Управление учётной записью текущего пользователя. */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Профиль и учётные данные пользователя")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(MeResponse.from(currentUserService.getCurrentUser()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Обновить профиль (ФИО, никнейм)")
    public ResponseEntity<MeResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User updated = userService.updateProfile(currentUserService.getCurrentUserId(),
                request.firstName(), request.lastName(), request.middleName(), request.nickname());
        return ResponseEntity.ok(MeResponse.from(updated));
    }

    @PutMapping("/email")
    @Operation(summary = "Сменить email (требует пароль, новый email требует подтверждения)")
    public ResponseEntity<MessageResponse> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        userService.updateEmail(currentUserService.getCurrentUserId(),
                request.newEmail(), request.password());
        return ResponseEntity.ok(MessageResponse.of("Email обновлён, подтвердите новый адрес"));
    }
}
