package com.simul_tech.netgenius.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simul_tech.netgenius.dto.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Тот же JSON-контракт ошибок, что у REST-контроллеров, для цепочки Spring Security. */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, "Требуется действительный access-токен");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        write(response, HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения операции");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(),
                ApiError.of(status.value(), status.getReasonPhrase(), message));
    }
}
