package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.ProfileResponse;
import com.simul_tech.netgenius.dto.SignUpRequest;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile controller", description = "API для личного кабинета")
@RequiredArgsConstructor
public class ProfileController {
    private final JwtCore jwtCore;
    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Получить информацию о пользователе по jwt-токену",
            description = "Принимает на вход токен и возвращает данные пользователя",
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
    public ResponseEntity<?> getInfo(String token) throws UsernameNotFoundException {
        String email = jwtCore.getEmailFromJwt(token);
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) userService.loadUserByUsername(email);
            ProfileResponse profileResponse = new ProfileResponse(
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getMiddleName(),
                    userDetails.getEmail()

            );
            return ResponseEntity.ok(profileResponse);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
