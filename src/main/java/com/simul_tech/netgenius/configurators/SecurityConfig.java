package com.simul_tech.netgenius.configurators;

import com.simul_tech.netgenius.security.SecurityErrorHandler;
import com.simul_tech.netgenius.security.TokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

/**
 * Конфигурация безопасности. RBAC задаётся централизованно по путям:
 * /admin/** — ADMIN, /teacher/** — TEACHER, /student/** и /sync/** — STUDENT.
 * Прочие защищённые ресурсы требуют аутентификации; публичные операции /auth
 * перечислены явно, также публичны GET /universities/** и документация.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenFilter tokenFilter;
    private final SecurityErrorHandler securityErrorHandler;

    private static final String[] PUBLIC_AUTH_POST = {
            "/auth/register", "/auth/login", "/auth/refresh", "/auth/logout",
            "/auth/confirm-email", "/auth/forgot-password", "/auth/reset-password", "/auth/resend-confirmation"
    };

    private static final String[] PUBLIC_DOCS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/api-docs.yaml"
    };

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
                    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
                    return configuration;
                }))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, PUBLIC_AUTH_POST).permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/ping", "/auth/confirm").permitAll()
                        .requestMatchers(PUBLIC_DOCS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/universities", "/universities/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/groups/join/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/groups/my").hasRole("STUDENT")
                        .requestMatchers("/groups", "/groups/**").hasRole("TEACHER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .requestMatchers("/sync/**").hasRole("STUDENT")
                        .anyRequest().authenticated())
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
