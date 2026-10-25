package com.simul_tech.netgenius.configurators;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${app.public-api-url:}")
    private String publicApiUrl;

    @Bean
    public OpenAPI api() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        OpenAPI api = new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", bearerScheme))
                .info(
                        new Info()
                                .title("Net-Genius API")
                                .description("API для системы Net-Genius")
                                .version("1.0.0")
                );
        // По умолчанию springdoc определяет адрес из запроса, включая доверенные proxy headers.
        // PUBLIC_API_URL позволяет явно задать внешний префикс, например /api.
        if (!publicApiUrl.isBlank()) {
            api.servers(List.of(new Server().url(publicApiUrl.trim()).description("API")));
        }
        return api;
    }
}
