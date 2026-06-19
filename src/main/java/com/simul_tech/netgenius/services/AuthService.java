package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.auth.MeResponse;
import com.simul_tech.netgenius.dto.auth.TokenResponse;
import com.simul_tech.netgenius.exceptions.InvalidTokenException;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.security.JwtCore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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

    @Value("${app.jwt.access-ttl-ms:900000}")
    private long accessTtlMs;

    public TokenResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtCore.generateAccessToken(principal);
        String refreshToken = refreshTokenService.issue(principal.getId());
        auditService.log(principal.getId(), "USER", principal.getId(), "LOGIN");
        return TokenResponse.bearer(accessToken, refreshToken, accessTtlMs);
    }

    public TokenResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(rawRefreshToken);
        User user = userService.findById(rotation.userId());
        String accessToken = jwtCore.generateAccessToken(UserDetailsImpl.build(user));
        return TokenResponse.bearer(accessToken, rotation.rawToken(), accessTtlMs);
    }

    public void logout(String rawRefreshToken) {
        try {
            Long userId = refreshTokenService.validate(rawRefreshToken).getUserId();
            refreshTokenService.revokeAllForUser(userId);
            auditService.log(userId, "USER", userId, "LOGOUT");
        } catch (InvalidTokenException e) {
            // Выход идемпотентен: недействительный токен трактуем как уже завершённую сессию.
            log.debug("Logout с недействительным refresh-токеном: {}", e.getMessage());
        }
    }

    public MeResponse me() {
        return MeResponse.from(currentUserService.getCurrentUser());
    }
}
