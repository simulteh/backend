package com.simul_tech.netgenius.configurators;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${server.port}")
    private int serverPort;

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .servers(
                        List.of(
                                new Server().url("http://localhost:" + serverPort),
                                new Server().url("http:212.67.12.82:" + serverPort)
                        )
                )
                .info(
                        new Info().title("Net-Genius API")
                );
    }
}
