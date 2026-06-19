package com.simul_tech.netgenius.security;

import com.simul_tech.netgenius.impls.UserDetailsImpl;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Генерация и разбор JWT access-токена. Время жизни access-токена — 15 минут.
 * Долгоживущая сессия обеспечивается refresh-токеном (см. RefreshTokenService).
 */
@Component
public class JwtCore {
    @Value("${testing.app.secret}")
    private String secret;

    /** Время жизни access-токена в мс (по умолчанию 15 минут). */
    @Value("${app.jwt.access-ttl-ms:900000}")
    private long accessTtlMs;

    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return generateAccessToken(userDetails);
    }

    public String generateAccessToken(UserDetailsImpl userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("uid", userDetails.getId())
                .claim("role", userDetails.getRole().name())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTtlMs))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getEmailFromJwt(String token) {
        return parse(token).getSubject();
    }

    public String getRoleFromJwt(String token) {
        return parse(token).get("role", String.class);
    }

    public Long getUserIdFromJwt(String token) {
        Object uid = parse(token).get("uid");
        return uid == null ? null : Long.valueOf(uid.toString());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
