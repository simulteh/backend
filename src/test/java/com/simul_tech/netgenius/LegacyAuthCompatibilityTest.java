package com.simul_tech.netgenius;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.RefreshTokenRepository;
import com.simul_tech.netgenius.repositories.UserRepository;
import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.services.MailSenderService;
import com.simul_tech.netgenius.services.RefreshTokenService;
import com.simul_tech.netgenius.services.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Старые HTTP-контракты проходят через текущую аутентификацию и настоящую цепочку безопасности. */
@SpringBootTest
@AutoConfigureMockMvc
class LegacyAuthCompatibilityTest {
    private static final String PASSWORD = "secret123";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository users;
    @Autowired private RefreshTokenRepository refreshTokens;
    @Autowired private RefreshTokenService refreshTokenService;
    @Autowired private TokenService tokenService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtCore jwtCore;
    @Value("${app.jwt.secret}") private String secret;
    @MockitoBean private MailSenderService mail;

    @Test
    void legacyRegistrationAndLoginPreserveStringResponsesAndCurrentTokenSecurity() throws Exception {
        String email = UUID.randomUUID() + "@example.test";
        String registration = mapper.writeValueAsString(Map.of(
                "email", email, "password", PASSWORD, "firstName", "Тест", "lastName", "Тестов"));
        mvc.perform(post("/auth/sign-up").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Пользователь зарегистрирован"));
        User user = users.findByEmail(email).orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isFalse();
        assertThat(passwordEncoder.matches(PASSWORD, user.getPassword())).isTrue();

        String accessToken = legacyLogin(email, PASSWORD);
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(accessToken).getBody();
        assertThat(jwtCore.getUserIdFromJwt(accessToken)).isEqualTo(user.getId());
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(900000);
        assertThat(refreshTokens.findAll()).noneMatch(token -> token.getUserId().equals(user.getId()));
        mvc.perform(get("/profile/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));
        mvc.perform(get("/admin/licenses").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/profile/me").param("token", accessToken))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/auth/sign-up").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пользователь с таким email уже существует"));
    }

    @Test
    void legacyLoginReturnsEmptyUnauthorizedForUnknownEmailAndWrongPassword() throws Exception {
        User user = createUser();
        for (String email : new String[]{user.getEmail(), "unknown-" + user.getEmail()}) {
            mvc.perform(post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(Map.of("email", email, "password", "wrong"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(""));
        }
        assertThat(refreshTokens.findAll()).noneMatch(token -> token.getUserId().equals(user.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/sign-up", "/auth/sign-in", "/auth/forgot", "/auth/reset"})
    void legacyEndpointsStillValidateBodies(String path) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void legacyForgotDoesNotDiscloseUnknownEmailAndSendsExistingUsersResetMail() throws Exception {
        User user = createUser();
        String expected = "If the email exists, a password reset link has been sent";
        for (String email : new String[]{user.getEmail(), "unknown-" + user.getEmail()}) {
            mvc.perform(post("/auth/forgot").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(Map.of("email", email))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(expected));
        }
        verify(mail).sendPasswordResetEmail(eq(user.getEmail()), anyString(), eq(user.getFirstName()));
    }

    @Test
    void legacyResetUsesSingleUseTokenAndRevokesRefreshSessions() throws Exception {
        User user = createUser();
        String refresh = refreshTokenService.issue(user.getId());
        String reset = tokenService.createResetToken(user.getId());
        String body = mapper.writeValueAsString(Map.of("token", reset, "newPassword", "changed123"));
        mvc.perform(post("/auth/reset").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));
        mvc.perform(post("/auth/reset").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isBadRequest());
        assertThat(legacyLogin(user.getEmail(), "changed123")).isNotBlank();
    }

    @Test
    void modernLoginStillReturnsBothTokensAsJson() throws Exception {
        User user = createUser();
        MvcResult result = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", user.getEmail(), "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();
        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(jwtCore.getUserIdFromJwt(response.get("accessToken").asText())).isEqualTo(user.getId());
        assertThat(refreshTokenService.rotate(response.get("refreshToken").asText()).userId()).isEqualTo(user.getId());
    }

    private String legacyLogin(String email, String password) throws Exception {
        return mvc.perform(post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andReturn().getResponse().getContentAsString();
    }

    private User createUser() {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.test");
        user.setFirstName("Тест");
        user.setLastName("Тестов");
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        return users.save(user);
    }
}
