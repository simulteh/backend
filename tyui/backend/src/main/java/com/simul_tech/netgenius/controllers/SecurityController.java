package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.ApiResponse;
import com.simul_tech.netgenius.models.ErrorResponse;
import com.simul_tech.netgenius.models.SignInRequest;
import com.simul_tech.netgenius.models.SignUpRequest;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.UserRepository;
import com.simul_tech.netgenius.security.JwtCore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "API для регистрации и авторизации пользователей")
@RequiredArgsConstructor
@Slf4j
public class SecurityController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с указанным именем пользователя, email и паролем. " +
                    "Если имя пользователя или email уже существуют, возвращает ошибку.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignUpRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример регистрации",
                                    value = "{\"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"password123\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешная регистрация пользователя",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешная регистрация",
                                    value = "{\"status\": \"success\", \"message\": \"Пользователь успешно зарегистрирован\", \"data\": null, \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка: имя пользователя или email уже существуют",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Дублирование имени пользователя",
                                    value = "{\"status\": 400, \"error\": \"BAD_REQUEST\", \"message\": \"Choose different name\", \"path\": \"/auth/sign-up\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<?> signup(@RequestBody SignUpRequest signupRequest) {
        log.info("Попытка регистрации пользователя: {}", signupRequest.getUsername());
        
        try {
            if (userRepository.existsByUsername(signupRequest.getUsername())) {
                log.warn("Попытка регистрации с существующим именем пользователя: {}", signupRequest.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different name");
            }
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                log.warn("Попытка регистрации с существующим email: {}", signupRequest.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
            }
            
            String hashed = passwordEncoder.encode(signupRequest.getPassword());

            User user = new User();
            user.setUsername(signupRequest.getUsername());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(hashed);
            userRepository.save(user);
            
            log.info("Пользователь успешно зарегистрирован: {}", signupRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Пользователь успешно зарегистрирован", null));
        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя {}: {}", signupRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при регистрации"));
        }
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Проверяет учетные данные пользователя и возвращает JWT токен, если авторизация успешна. " +
                    "Возвращает статус 401, если учетные данные неверны.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для авторизации",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignInRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример авторизации",
                                    value = "{\"username\": \"john_doe\", \"password\": \"password123\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешная авторизация, возвращает JWT токен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешная авторизация",
                                    value = "{\"status\": \"success\", \"message\": \"Авторизация успешна\", \"data\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Ошибка: неверные учетные данные",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Неверные учетные данные",
                                    value = "{\"status\": 401, \"error\": \"UNAUTHORIZED\", \"message\": \"Неверные учетные данные\", \"path\": \"/auth/sign-in\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<?> signin(@RequestBody SignInRequest signinRequest) {
        log.info("Попытка авторизации пользователя: {}", signinRequest.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtCore.generateToken(authentication);
            
            log.info("Пользователь успешно авторизован: {}", signinRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Авторизация успешна", jwt));
        } catch (BadCredentialsException e) {
            log.warn("Неверные учетные данные для пользователя: {}", signinRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "UNAUTHORIZED", "Неверные учетные данные", "/auth/sign-in"));
        } catch (Exception e) {
            log.error("Ошибка при авторизации пользователя {}: {}", signinRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при авторизации"));
        }
    }
}