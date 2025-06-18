package com.simul_tech.netgenius.configurators;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Net-Genius API")
                        .description("REST API для системы управления образовательным процессом Net-Genius. " +
                                "Включает функциональность для работы с пользователями, преподавателями, " +
                                "аутентификации и авторизации.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Net-Genius Team")
                                .email("support@netgenius.com")
                                .url("https://netgenius.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Локальный сервер разработки"),
                        new Server()
                                .url("https://api.netgenius.com")
                                .description("Продакшн сервер")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("JWT токен для аутентификации. Получите токен через эндпоинт /auth/sign-in");
    }
}
