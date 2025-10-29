package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.PasswordResetDTO;
import com.simul_tech.netgenius.dto.PasswordResetRequest;
import com.simul_tech.netgenius.dto.SignInRequest;
import com.simul_tech.netgenius.dto.SignUpRequest;
import com.simul_tech.netgenius.exceptions.ExpiredTokenException;
import com.simul_tech.netgenius.exceptions.InvalidTokenException;
import com.simul_tech.netgenius.exceptions.UserNotFoundException;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.services.PasswordResetService;
import com.simul_tech.netgenius.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для регистрации и авторизации пользователей")
public class SecurityController {
    private final JwtCore jwtCore;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetService passwordResetService;

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с указанными данными. Email должен быть уникальным.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = """
                                            {
                                                "firstName": "Иван",
                                                "lastName": "Иванов",
                                                "middleName": "Иванович",
                                                "email": "ivanov@example.com",
                                                "password": "password123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная регистрация",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = "Пользователь зарегистрирован"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Пользователь с таким email уже существует",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = "Пользователь с таким email уже существует"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера"
                    )
            }
    )
    ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest signupRequest) {
        if (userService.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Пользователь с таким email уже существует");
        }
        String hashed = passwordEncoder.encode(signupRequest.getPassword());

        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setMiddleName(signupRequest.getMiddleName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashed);
        userService.save(user);
        return ResponseEntity.ok("Пользователь зарегистрирован");
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Аутентификация пользователя по email и паролю. Возвращает JWT токен для доступа к защищенным endpoints.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные пользователя",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignInRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = """
                                            {
                                                "email": "ivanov@example.com",
                                                "password": "password123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная авторизация",
                            content = @Content(
                                    examples = @ExampleObject(
                                            name = "Пример ответа",
                                            value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неверные учетные данные"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера"
                    )
            }
    )
    ResponseEntity<?> signin(@Valid @RequestBody SignInRequest signinRequest) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signinRequest.getEmail(),
                            signinRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.createPasswordResetToken(request.getEmail());
            return ResponseEntity.ok().body(
                    Map.of("message", "If the email exists, a password reset link has been sent")
            );

        } catch (Exception e) {
            log.error("Error in password reset request for email: {}", request.getEmail(), e);
            return ResponseEntity.ok().body(
                    Map.of("message", "If the email exists, a password reset link has been sent")
            );
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDTO request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

            return ResponseEntity.ok().body(
                    Map.of("message", "Password has been reset successfully")
            );

        } catch (InvalidTokenException | ExpiredTokenException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid or expired reset token")
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "User not found")
            );
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to reset password")
            );
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(
                Map.of("error", "Validation failed", "details", errors)
        );
    }
}