package com.simul_tech.netgenius.security;

import com.simul_tech.netgenius.impls.UserDetailsImpl;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtCore {

    @Value("${student.app.jwtSecret}")
    private String jwtSecret;

    @Value("${student.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Генерирует JWT токен для аутентифицированного пользователя
     * @param authentication объект аутентификации Spring Security
     * @return JWT токен
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Извлекает имя пользователя из JWT токена
     * @param token JWT токен
     * @return имя пользователя
     * @throws JwtException если токен невалиден
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Проверяет валидность JWT токена
     * @param token JWT токен
     * @return true если токен валиден, false в противном случае
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            // Неверная подпись
        } catch (MalformedJwtException e) {
            // Невалидный токен
        } catch (ExpiredJwtException e) {
            // Токен просрочен
        } catch (UnsupportedJwtException e) {
            // Неподдерживаемый токен
        } catch (IllegalArgumentException e) {
            // Пустой токен
        }
        return false;
    }
}
