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
@Tag(name = "Аутентификация", description = "API для авторизации и аутентификации в системе управления студентами")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя для доступа к системе управления студентами",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignUpRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = "{\"username\": \"admin\", \"email\": \"admin@university.edu\", \"password\": \"securePassword123\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"success\", \"message\": \"User registered successfully\", \"data\": null}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные или пользователь уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody SignUpRequest signUpRequest) {
        log.info("Attempt to register new user: {}", signUpRequest.getUsername());

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            log.warn("Username already exists: {}", signUpRequest.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username is already taken"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("Email already exists: {}", signUpRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is already in use"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        userRepository.save(user);

        log.info("User registered successfully: {}", signUpRequest.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", null));
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Аутентифицирует пользователя и возвращает JWT токен для доступа к API",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные пользователя",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignInRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = "{\"username\": \"admin\", \"password\": \"securePassword123\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Аутентификация успешна",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": \"success\", \"message\": \"Authentication successful\", \"data\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Неверные учетные данные",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<String>> signIn(@RequestBody SignInRequest signInRequest) {
        log.info("Attempt to authenticate user: {}", signInRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signInRequest.getUsername(),
                            signInRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtCore.generateToken(authentication);

            log.info("User authenticated successfully: {}", signInRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Authentication successful", jwt));
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", signInRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials"));
        }
    }
}