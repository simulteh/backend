package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.PasswordResetDTO;
import com.simul_tech.netgenius.dto.PasswordResetRequest;
import com.simul_tech.netgenius.dto.SignInRequest;
import com.simul_tech.netgenius.dto.SignUpRequest;
import com.simul_tech.netgenius.dto.auth.*;
import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.services.AuthService;
import com.simul_tech.netgenius.services.CurrentUserService;
import com.simul_tech.netgenius.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Аутентификация и управление сессией. JWT access-токен живёт 15 минут,
 * refresh-токен — 30 дней.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Регистрация, вход, обновление токена, подтверждение email")
public class SecurityController {
    private final UserService userService;
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @GetMapping("/ping")
    @Operation(summary = "Проверка доступности сервиса")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя (роль USER, требуется подтверждение email)")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody SignUpRequest request) {
        userService.signUpUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.of("Пользователь зарегистрирован, подтвердите email"));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход: возвращает access- и refresh-токены")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление пары токенов по refresh-токену (с ротацией)")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход: отзывает refresh-токены пользователя")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(MessageResponse.of("Выход выполнен"));
    }

    @PostMapping("/confirm-email")
    @Operation(summary = "Подтверждение email по токену из письма")
    public ResponseEntity<MessageResponse> confirmEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        userService.confirmUser(request.token());
        return ResponseEntity.ok(MessageResponse.of("Email подтверждён"));
    }

    @GetMapping("/confirm")
    @Operation(summary = "Подтверждение email по ссылке (token в query)")
    public ResponseEntity<MessageResponse> confirmByLink(@RequestParam("token") String token) {
        userService.confirmUser(token);
        return ResponseEntity.ok(MessageResponse.of("Email подтверждён"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Смена пароля авторизованным пользователем",
            security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUserService.getCurrentUserId(),
                request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(MessageResponse.of("Пароль изменён"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Запрос на сброс пароля (письмо со ссылкой)")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            userService.sendResetToken(request.getEmail());
        } catch (Exception e) {
            // Не раскрываем существование email.
            log.debug("forgot-password для {} завершился: {}", request.getEmail(), e.getMessage());
        }
        return ResponseEntity.ok(MessageResponse.of("Если email существует, ссылка для сброса отправлена"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Установка нового пароля по токену сброса")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetDTO request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.of("Пароль успешно сброшен"));
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь по access-токену",
            security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
