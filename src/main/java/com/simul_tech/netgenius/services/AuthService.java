package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.auth.MeResponse;
import com.simul_tech.netgenius.dto.auth.TokenResponse;
import com.simul_tech.netgenius.exceptions.EmailNotFound;
import com.simul_tech.netgenius.exceptions.InvalidTokenException;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.security.JwtCore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Оркестрация аутентификации: логин (access+refresh), обновление токена,
 * выход (отзыв refresh-токенов) и сведения о текущем пользователе.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public TokenResponse login(String email, String password) {
        UserDetailsImpl principal = authenticate(email, password);

        String accessToken = jwtCore.generateAccessToken(principal);
        String refreshToken = refreshTokenService.issue(principal.getId());
        auditService.log(principal.getId(), "USER", principal.getId(), "LOGIN");
        return TokenResponse.bearer(accessToken, refreshToken, jwtCore.getAccessTtlMs());
    }

    /** Старый клиент принимает только строку JWT и не умеет сохранять refresh-токен. */
    public String loginLegacyAccessToken(String email, String password) {
        UserDetailsImpl principal = authenticate(email, password);
        String accessToken = jwtCore.generateAccessToken(principal);
        auditService.log(principal.getId(), "USER", principal.getId(), "LOGIN");
        return accessToken;
    }

    private UserDetailsImpl authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(rawRefreshToken);
        User user = userService.findById(rotation.userId());
        String accessToken = jwtCore.generateAccessToken(UserDetailsImpl.build(user));
        return TokenResponse.bearer(accessToken, rotation.rawToken(), jwtCore.getAccessTtlMs());
    }

    public void logout(String rawRefreshToken) {
        try {
            Long userId = refreshTokenService.revokeAllForToken(rawRefreshToken);
            auditService.log(userId, "USER", userId, "LOGOUT");
        } catch (InvalidTokenException e) {
            // Выход идемпотентен: недействительный токен трактуем как уже завершённую сессию.
            log.debug("Logout с недействительным refresh-токеном: {}", e.getMessage());
        }
    }

    public MeResponse me() {
        return MeResponse.from(currentUserService.getCurrentUser());
    }

    public void forgotPassword(String email) {
        try {
            userService.sendResetToken(email);
        } catch (EmailNotFound e) {
            // Публичный ответ одинаков для существующего и неизвестного адреса.
            log.debug("Password reset requested for an unknown email");
        }
    }

    public void resendConfirmation(String email) {
        try {
            User user = userService.findByEmail(email);
            if (!user.isEnabled()) {
                userService.sendVerifyToken(email);
            }
        } catch (EmailNotFound e) {
            log.debug("Confirmation requested for an unknown email");
        }
    }
}
