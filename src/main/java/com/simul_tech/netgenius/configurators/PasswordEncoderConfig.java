package com.simul_tech.netgenius.configurators;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Хеширование паролей. По требованию безопасности новые пароли хешируются Argon2.
 * Используется DelegatingPasswordEncoder: новые пароли получают префикс {argon2},
 * а исторические хеши без префикса трактуются как bcrypt — для обратной совместимости.
 */
@Configuration
public class PasswordEncoderConfig {

    private static final String DEFAULT_ID = "argon2";

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        DelegatingPasswordEncoder encoder = new DelegatingPasswordEncoder(DEFAULT_ID, encoders);
        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return encoder;
    }
}
