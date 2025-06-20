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
                        .title("Student Management API")
                        .description("REST API для управления данными студентов. " +
                                "Включает функциональность для CRUD-операций со студентами, " +
                                "фильтрацию по различным параметрам и интеграцию с системой аутентификации.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("support@university.edu")
                                .url("https://university.edu"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Локальный сервер разработки"),
                        new Server()
                                .url("https://api.university.edu/student-management")
                                .description("Продакшн сервер")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .name("Authorization")
                .description("Для доступа к API необходимо получить JWT токен через эндпоинт /auth/login " +
                        "и добавить его в заголовок запроса: 'Authorization: Bearer <token>'");
    }
}