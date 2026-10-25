package com.simul_tech.netgenius.security;

import com.simul_tech.netgenius.impls.UserDetailsImpl;
import io.jsonwebtoken.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Генерация и разбор JWT access-токена. Время жизни access-токена — 15 минут.
 * Долгоживущая сессия обеспечивается refresh-токеном (см. RefreshTokenService).
 */
@Component
public class JwtCore {
    @Value("${app.jwt.secret}")
    private String secret;

    /** Время жизни access-токена в мс (по умолчанию 15 минут). */
    @Value("${app.jwt.access-ttl-ms:900000}")
    @Getter
    private long accessTtlMs;

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

    public Long getUserIdFromJwt(String token) {
        Claims claims = parse(token);
        Object uid = claims.get("uid");
        if (uid == null || claims.getExpiration() == null) {
            throw new MalformedJwtException("Access token requires uid and expiration");
        }
        Long userId = Long.valueOf(uid.toString());
        if (userId <= 0) {
            throw new MalformedJwtException("Invalid user id");
        }
        return userId;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
