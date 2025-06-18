package com.simul_tech.netgenius.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenFilter extends OncePerRequestFilter {
    private final JwtCore jwtCore;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwt = null;
        String username = null;
        UserDetails userDetails;
        UsernamePasswordAuthenticationToken auth;
        
        try {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7);
                log.debug("JWT токен найден в заголовке Authorization");
            } else {
                log.debug("JWT токен не найден в заголовке Authorization");
            }
            
            if (jwt != null) {
                try {
                    username = jwtCore.getNameFromJwt(jwt);
                    log.debug("Извлечено имя пользователя из JWT: {}", username);
                } catch (ExpiredJwtException e) {
                    log.warn("JWT токен истек для запроса: {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
                    return;
                } catch (Exception e) {
                    log.error("Ошибка при парсинге JWT токена: {}", e.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                        auth = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Аутентификация установлена для пользователя: {}", username);
                    } catch (Exception e) {
                        log.error("Ошибка при загрузке пользователя {}: {}", username, e.getMessage());
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        return;
                    }
                } else if (username != null) {
                    log.debug("Аутентификация уже установлена для пользователя: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Неожиданная ошибка в TokenFilter: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
