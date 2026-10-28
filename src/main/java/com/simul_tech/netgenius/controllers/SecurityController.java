package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.PasswordResetDTO;
import com.simul_tech.netgenius.dto.PasswordResetRequest;
import com.simul_tech.netgenius.dto.SignInRequest;
import com.simul_tech.netgenius.dto.SignUpRequest;
import com.simul_tech.netgenius.dto.auth.*;
import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.common.ApiError;
import com.simul_tech.netgenius.exceptions.InvalidRequestException;
import com.simul_tech.netgenius.services.AuthService;
import com.simul_tech.netgenius.services.CurrentUserService;
import com.simul_tech.netgenius.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

/**
 * Аутентификация и управление сессией. JWT access-токен живёт 15 минут,
 * refresh-токен — 30 дней.
 */
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован; роль USER"),
            @ApiResponse(responseCode = "400", description = "Некорректные поля или email уже зарегистрирован", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody SignUpRequest request) {
        userService.signUpUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.of("Пользователь зарегистрирован, подтвердите email"));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход: возвращает access- и refresh-токены")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пара токенов; вход одинаков для всех ролей, включая ADMIN"),
            @ApiResponse(responseCode = "400", description = "Некорректное тело запроса", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Совместимость со старым клиентом: регистрация; используйте /auth/register", deprecated = true)
    public ResponseEntity<String> legacyRegister(@Valid @RequestBody SignUpRequest request) {
        try {
            userService.signUpUser(request);
            return ResponseEntity.ok("Пользователь зарегистрирован");
        } catch (InvalidRequestException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Совместимость со старым клиентом: JWT строкой; используйте /auth/login", deprecated = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access-токен строкой"),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль; тело ответа пустое", content = @Content)
    })
    public ResponseEntity<String> legacyLogin(@Valid @RequestBody SignInRequest request) {
        try {
            return ResponseEntity.ok(authService.loginLegacyAccessToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            // Старый клиент хранит тело успешного ответа как строку и ожидает пустой 401.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление пары токенов по refresh-токену (с ротацией)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Новая пара токенов; прежний refresh-токен больше не действует"),
            @ApiResponse(responseCode = "400", description = "Некорректный, истёкший, отозванный или уже использованный refresh-токен", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход: отзывает refresh-токены пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Выход выполнен; уже недействительный refresh-токен также даёт 200"),
            @ApiResponse(responseCode = "400", description = "Refresh-токен не передан или пуст", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(MessageResponse.of("Выход выполнен"));
    }

    @PostMapping("/confirm-email")
    @Operation(summary = "Подтверждение email по токену из письма")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email подтверждён; роль пользователя не меняется"),
            @ApiResponse(responseCode = "400", description = "Токен пуст, истёк или уже использован", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Токен не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> confirmEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        userService.confirmUser(request.token());
        return ResponseEntity.ok(MessageResponse.of("Email подтверждён"));
    }

    @GetMapping("/confirm")
    @Operation(summary = "Подтверждение email по ссылке (token в query)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email подтверждён; роль пользователя не меняется"),
            @ApiResponse(responseCode = "400", description = "Токен истёк или уже использован", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Токен не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> confirmByLink(@RequestParam("token") String token) {
        userService.confirmUser(token);
        return ResponseEntity.ok(MessageResponse.of("Email подтверждён"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Смена пароля авторизованным пользователем",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль изменён; refresh-токены отозваны"),
            @ApiResponse(responseCode = "400", description = "Некорректные поля или текущий пароль неверен", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Требуется действующий access-токен", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUserService.getCurrentUserId(),
                request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(MessageResponse.of("Пароль изменён"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Запрос на сброс пароля (письмо со ссылкой)",
            description = "Для существующего адреса письмо сохраняется в очередь и отправляется после фиксации транзакции. При временном сбое SMTP отправка повторяется автоматически. Ответ не раскрывает существование адреса и не подтверждает доставку.")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of("Если email существует, ссылка для сброса отправлена"));
    }

    @PostMapping("/forgot")
    @Operation(summary = "Совместимость со старым клиентом: запрос сброса; используйте /auth/forgot-password", deprecated = true)
    public ResponseEntity<MessageResponse> legacyForgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of("If the email exists, a password reset link has been sent"));
    }

    @PostMapping("/resend-confirmation")
    @Operation(summary = "Повторно отправить подтверждение email; существование адреса не раскрывается",
            description = "Письмо сохраняется в очередь с автоматическими повторами при сбое SMTP. Прежняя доставленная ссылка действует до успешной отправки новой. Существование адреса и результат доставки в публичном ответе не раскрываются.")
    public ResponseEntity<MessageResponse> resendConfirmation(@Valid @RequestBody PasswordResetRequest request) {
        authService.resendConfirmation(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of("Если email существует и не подтверждён, письмо отправлено"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Установка нового пароля по токену сброса")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль изменён; refresh-токены отозваны"),
            @ApiResponse(responseCode = "400", description = "Некорректные поля; токен истёк, имеет другой тип или уже использован", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Токен не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetDTO request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.of("Пароль успешно сброшен"));
    }

    @PostMapping("/reset")
    @Operation(summary = "Совместимость со старым клиентом: новый пароль; используйте /auth/reset-password", deprecated = true)
    public ResponseEntity<MessageResponse> legacyResetPassword(@Valid @RequestBody PasswordResetDTO request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.of("Password has been reset successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь по access-токену",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Текущий пользователь и его роль"),
            @ApiResponse(responseCode = "401", description = "Требуется действующий access-токен", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
