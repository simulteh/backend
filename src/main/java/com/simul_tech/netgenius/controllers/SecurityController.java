package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.SignInRequest;
import com.simul_tech.netgenius.models.SignUpRequest;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.UserRepository;
import com.simul_tech.netgenius.security.JwtCore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Security")
@RequiredArgsConstructor
public class SecurityController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с указанным именем пользователя, email и паролем " +
                    "Если имя пользователя или email уже существуют, возвращает ошибку",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная регистрация пользователя"),
                    @ApiResponse(responseCode = "400", description = "Ошибка: пользователь с таким email уже существует"),
                    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера")
            }
    )
    ResponseEntity<?> signup(@RequestBody SignUpRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Пользователь с таким email уже существует");
        }
        String hashed = passwordEncoder.encode(signupRequest.getPassword());

        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setMiddleName(signupRequest.getMiddleName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashed);
        userRepository.save(user);
        return ResponseEntity.ok("Пользователь зарегистрирован");
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Проверяет учетные данные пользователя и возвращает JWT токен, если авторизация успешна " +
                    "Возвращает статус 401, если учетные данные неверны",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Авторизация прошла успешна, выдан JWT токен"),
                    @ApiResponse(responseCode = "401", description = "Ошибка: неверные учетные данные"),
                    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера")
            }
    )
    ResponseEntity<?> signin(@RequestBody SignInRequest signinRequest) {
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
}