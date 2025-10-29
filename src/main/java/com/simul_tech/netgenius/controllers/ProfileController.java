package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.ProfileResponse;
import com.simul_tech.netgenius.exceptions.EmailNotFound;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Tag(name = "Profile controller", description = "API для личного кабинета")
public class ProfileController {
    private final JwtCore jwtCore;
    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Получить информацию о пользователе по jwt-токену",
            description = "Читает JWT-токен из заголовка Authorization (Bearer) и возвращает данные пользователя",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная регистрация",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                                "firstName": "Иван",
                                                "lastName": "Иванов",
                                                "middleName": "Иванович",
                                                "email": "ivanov@example.com",
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Пользователь не найден"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера"
                    )
            }
    )
        public ResponseEntity<?> getInfo(HttpServletRequest request) {
                String authorizationHeader = request.getHeader("Authorization");
    
                if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
                }
    
                String token = authorizationHeader.substring(7);
                String email = jwtCore.getEmailFromJwt(token);
        try {
            User user = userService.findByEmail(email);
            ProfileResponse profileResponse = new ProfileResponse(
                    user.getFirstName(),
                    user.getLastName(),
                    user.getMiddleName(),
                    user.getEmail()
            );
            return ResponseEntity.ok(profileResponse);
        } catch (EmailNotFound e) {
            log.info("User with email not found {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
