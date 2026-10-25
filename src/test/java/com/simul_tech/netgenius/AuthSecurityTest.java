package com.simul_tech.netgenius;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.exceptions.InvalidTokenException;
import com.simul_tech.netgenius.exceptions.TokenAlreadyUsedException;
import com.simul_tech.netgenius.impls.TokenType;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.RefreshToken;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.RefreshTokenRepository;
import com.simul_tech.netgenius.repositories.UserRepository;
import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.services.AuthService;
import com.simul_tech.netgenius.services.RefreshTokenService;
import com.simul_tech.netgenius.services.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Регрессии аутентификации через настоящие фильтры, транзакции и H2. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityTest {
    private static final String PASSWORD = "secret123";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository users;
    @Autowired private RefreshTokenRepository refreshTokens;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtCore jwtCore;
    @Autowired private AuthService authService;
    @Autowired private RefreshTokenService refreshTokenService;
    @Autowired private TokenService tokenService;
    @Autowired private PlatformTransactionManager transactionManager;
    @Value("${testing.app.secret}") private String secret;

    @Test
    void unknownEmailAndWrongPasswordReturnSameUnauthorizedResponse() throws Exception {
        User user = createUser(Role.USER);
        JsonNode unknown = loginError("missing-" + user.getEmail(), PASSWORD);
        JsonNode wrongPassword = loginError(user.getEmail(), "incorrect");
        assertThat(unknown.get("message")).isEqualTo(wrongPassword.get("message"));
        assertThat(unknown.get("error").asText()).isEqualTo("Unauthorized");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/me", "/auth/change-password", "/auth/future-protected-operation", "/admin/licenses"})
    void protectedEndpointsReturnJsonUnauthorized(String path) throws Exception {
        var request = path.equals("/auth/change-password") ? post(path) : get(path);
        mvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void accessTokenRemainsBoundToOriginalUserWhenEmailIsReused() throws Exception {
        User original = createUser(Role.USER);
        String oldEmail = original.getEmail();
        String accessToken = accessToken(original);
        String newEmail = "new-" + oldEmail;

        mvc.perform(put("/users/email").header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("newEmail", newEmail, "password", PASSWORD))))
                .andExpect(status().isOk());

        User replacement = createUser(Role.ADMIN);
        replacement.setEmail(oldEmail);
        users.save(replacement);

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(original.getId()))
                .andExpect(jsonPath("$.email").value(newEmail))
                .andExpect(jsonPath("$.emailConfirmed").value(false));
        mvc.perform(get("/admin/licenses").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void deletedUsersTokenCannotAuthenticateReplacementWithSameEmail() throws Exception {
        User original = createUser(Role.USER);
        String token = accessToken(original);
        users.deleteById(original.getId());
        User replacement = createUser(Role.USER);
        replacement.setEmail(original.getEmail());
        users.save(replacement);
        expectInvalidAccessToken(token);
    }

    @Test
    void authorizationUsesCurrentDatabaseRoleInsteadOfTokenClaim() throws Exception {
        User user = createUser(Role.ADMIN);
        String token = accessToken(user);
        user.setRole(Role.USER);
        users.save(user);
        mvc.perform(get("/admin/licenses").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void malformedExpiredUnsignedAndIncorrectlySignedAccessTokensAreRejected() throws Exception {
        User user = createUser(Role.USER);
        expectInvalidAccessToken("");
        expectInvalidAccessToken("not-a-jwt");
        expectInvalidAccessToken(Jwts.builder().setSubject(user.getEmail()).claim("uid", user.getId())
                .setExpiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(SignatureAlgorithm.HS512, secret).compact());
        expectInvalidAccessToken(Jwts.builder().setSubject(user.getEmail()).claim("uid", user.getId())
                .setExpiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(SignatureAlgorithm.HS512, "different-signing-secret").compact());
        expectInvalidAccessToken(Jwts.builder().setSubject(user.getEmail()).claim("uid", user.getId())
                .setExpiration(Date.from(Instant.now().plusSeconds(60))).compact());
    }

    @Test
    void accessTokensRequireValidIdAndExpiration() throws Exception {
        User user = createUser(Role.USER);
        expectInvalidAccessToken(Jwts.builder().setSubject(user.getEmail())
                .setExpiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(SignatureAlgorithm.HS512, secret).compact());
        expectInvalidAccessToken(Jwts.builder().setSubject(user.getEmail()).claim("uid", user.getId())
                .signWith(SignatureAlgorithm.HS512, secret).compact());
        for (Object id : new Object[]{0, -1, "invalid", 1.5}) {
            expectInvalidAccessToken(Jwts.builder().setSubject(user.getEmail()).claim("uid", id)
                    .setExpiration(Date.from(Instant.now().plusSeconds(60)))
                    .signWith(SignatureAlgorithm.HS512, secret).compact());
        }
    }

    @Test
    void refreshRotatesTokenAndRejectsReuse() throws Exception {
        User user = createUser(Role.USER);
        String initial = refreshTokenService.issue(user.getId());
        MvcResult result = mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", initial))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInMs").value(900000))
                .andReturn();
        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("refreshToken").asText()).isNotEqualTo(initial);
        expectRefreshRejected(initial);
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + response.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));
        assertThat(refreshTokenService.rotate(response.get("refreshToken").asText()).userId()).isEqualTo(user.getId());
    }

    @Test
    void expiredAndUnknownRefreshTokensAreRejected() throws Exception {
        User user = createUser(Role.USER);
        String raw = refreshTokenService.issue(user.getId());
        RefreshToken token = refreshTokens.findByTokenHash(RefreshToken.hash(raw)).orElseThrow();
        token.setExpiresAt(Instant.now().minusSeconds(1));
        refreshTokens.save(token);
        expectRefreshRejected(raw);
        expectRefreshRejected("unknown-refresh-token");
    }

    @Test
    void logoutIsIdempotentAndRevokesAllOwnersSessionsOnly() throws Exception {
        User owner = createUser(Role.USER);
        String first = refreshTokenService.issue(owner.getId());
        String second = refreshTokenService.issue(owner.getId());
        User other = createUser(Role.USER);
        String unrelated = refreshTokenService.issue(other.getId());
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(Map.of("refreshToken", first))))
                    .andExpect(status().isOk());
        }
        expectRefreshRejected(first);
        expectRefreshRejected(second);
        assertThat(refreshTokenService.rotate(unrelated).userId()).isEqualTo(other.getId());
    }

    @Test
    void passwordChangeRevokesRefreshTokensAndOutstandingResetLinks() throws Exception {
        User user = createUser(Role.USER);
        String refresh = refreshTokenService.issue(user.getId());
        String reset = tokenService.createResetToken(user.getId());
        mvc.perform(post("/auth/change-password").header("Authorization", "Bearer " + accessToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("oldPassword", PASSWORD, "newPassword", "changed123"))))
                .andExpect(status().isOk());
        expectRefreshRejected(refresh);
        assertThrows(TokenAlreadyUsedException.class, () -> tokenService.consumeToken(reset, TokenType.RESET));
        loginError(user.getEmail(), PASSWORD);
        assertThat(authService.login(user.getEmail(), "changed123").accessToken()).isNotBlank();
    }

    @Test
    void passwordResetIsSingleUseAndRevokesRefreshTokens() throws Exception {
        User user = createUser(Role.USER);
        String refresh = refreshTokenService.issue(user.getId());
        String reset = tokenService.createResetToken(user.getId());
        String body = mapper.writeValueAsString(Map.of("token", reset, "newPassword", "reset123"));
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        expectRefreshRejected(refresh);
        assertThat(authService.login(user.getEmail(), "reset123").accessToken()).isNotBlank();
    }

    @Test
    void wrongPasswordDoesNotRevokeExistingSession() throws Exception {
        User user = createUser(Role.USER);
        String refresh = refreshTokenService.issue(user.getId());
        mvc.perform(post("/auth/change-password").header("Authorization", "Bearer " + accessToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("oldPassword", "wrong", "newPassword", "changed123"))))
                .andExpect(status().isBadRequest());
        assertThat(refreshTokenService.rotate(refresh).userId()).isEqualTo(user.getId());
    }

    @Test
    void confirmationChecksTokenTypeAndRejectsReuse() throws Exception {
        User user = createUser(Role.USER);
        String verify = tokenService.createVerifyToken(user.getId());
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("token", verify, "newPassword", "changed123"))))
                .andExpect(status().isBadRequest());
        String body = mapper.writeValueAsString(Map.of("token", verify));
        mvc.perform(post("/auth/confirm-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/confirm-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        assertThat(users.findById(user.getId()).orElseThrow().isEnabled()).isTrue();
    }

    @Test
    void forgotPasswordDoesNotDiscloseUnknownEmail() throws Exception {
        mvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Если email существует, ссылка для сброса отправлена"));
    }

    @Test
    void failedRefreshTransactionRollsBackRotation() {
        User user = createUser(Role.USER);
        String raw = refreshTokenService.issue(user.getId());
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager).execute(status -> {
            authService.refresh(raw);
            throw new IllegalStateException("Rollback after rotation");
        }));
        assertThat(refreshTokens.findByTokenHash(RefreshToken.hash(raw)).orElseThrow().isRevoked()).isFalse();
        assertThat(refreshTokens.findAll().stream().filter(token -> token.getUserId().equals(user.getId()))).hasSize(1);
    }

    @Test
    void concurrentRefreshCannotUseSameTokenTwice() throws Exception {
        User user = createUser(Role.USER);
        String raw = refreshTokenService.issue(user.getId());
        assertSingleUse(() -> refreshTokenService.rotate(raw), InvalidTokenException.class);
    }

    @Test
    void concurrentResetCannotConsumeSameTokenTwice() throws Exception {
        User user = createUser(Role.USER);
        String raw = tokenService.createResetToken(user.getId());
        assertSingleUse(() -> tokenService.consumeToken(raw, TokenType.RESET), TokenAlreadyUsedException.class);
    }

    @Test
    void logoutWaitsForRotationOfAnotherSessionAndRevokesItsSuccessor() throws Exception {
        User user = createUser(Role.USER);
        String rotating = refreshTokenService.issue(user.getId());
        String loggingOut = refreshTokenService.issue(user.getId());
        var executor = Executors.newFixedThreadPool(2);
        var rotated = new CountDownLatch(1);
        var releaseRotation = new CountDownLatch(1);
        var logoutStarted = new CountDownLatch(1);
        try {
            var rotation = executor.submit(() -> new TransactionTemplate(transactionManager).execute(status -> {
                String next = refreshTokenService.rotate(rotating).rawToken();
                rotated.countDown();
                try {
                    assertTrue(releaseRotation.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
                return next;
            }));
            assertTrue(rotated.await(5, TimeUnit.SECONDS));
            var logout = executor.submit(() -> {
                logoutStarted.countDown();
                authService.logout(loggingOut);
            });
            assertTrue(logoutStarted.await(5, TimeUnit.SECONDS));
            try {
                assertThrows(TimeoutException.class, () -> logout.get(250, TimeUnit.MILLISECONDS));
            } finally {
                releaseRotation.countDown();
            }
            String next = rotation.get(5, TimeUnit.SECONDS);
            logout.get(5, TimeUnit.SECONDS);
            expectRefreshRejected(next);
            expectRefreshRejected(rotating);
            expectRefreshRejected(loggingOut);
        } finally {
            releaseRotation.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private User createUser(Role role) {
        User user = new User();
        user.setFirstName("Тест");
        user.setLastName("Тестов");
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        return users.save(user);
    }

    private String accessToken(User user) {
        return jwtCore.generateAccessToken(UserDetailsImpl.build(user));
    }

    private JsonNode loginError(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    private void expectInvalidAccessToken(String token) throws Exception {
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    private void expectRefreshRejected(String token) throws Exception {
        // Сохраняем существующий контракт /auth/refresh: ошибки refresh-токена — 400.
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", token))))
                .andExpect(status().isBadRequest());
    }

    /** Удерживаем первую транзакцию открытой, чтобы второй запрос гарантированно конкурировал с ней. */
    private void assertSingleUse(Supplier<?> operation, Class<? extends RuntimeException> rejection) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var firstUsed = new CountDownLatch(1);
        var releaseFirst = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        try {
            var first = executor.submit(() -> new TransactionTemplate(transactionManager).execute(status -> {
                Object result = operation.get();
                firstUsed.countDown();
                try {
                    assertTrue(releaseFirst.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
                return result;
            }));
            assertTrue(firstUsed.await(5, TimeUnit.SECONDS));
            var second = executor.submit(() -> {
                secondStarted.countDown();
                return operation.get();
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            try {
                assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS));
            } finally {
                releaseFirst.countDown();
            }
            assertThat(first.get(5, TimeUnit.SECONDS)).isNotNull();
            ExecutionException failure = assertThrows(ExecutionException.class, () -> second.get(5, TimeUnit.SECONDS));
            assertThat(failure.getCause()).isInstanceOf(rejection);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
