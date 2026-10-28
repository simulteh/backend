package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.*;
import com.simul_tech.netgenius.impls.TokenType;
import com.simul_tech.netgenius.models.ResetToken;
import com.simul_tech.netgenius.models.Token;
import com.simul_tech.netgenius.models.VerifyToken;
import com.simul_tech.netgenius.impls.TokenDetails;
import com.simul_tech.netgenius.repositories.TokenRepository;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    private static final Duration VERIFY_TOKEN_TTL = Duration.ofHours(24);
    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(24);

    @Transactional
    public String createVerifyToken(Long userId) {
        cancelActiveTokens(userId, TokenType.VERIFY);

        VerifyToken token = new VerifyToken(userId, VERIFY_TOKEN_TTL);
        tokenRepository.save(token);
        return token.getRawToken();
    }

    @Transactional
    public String createResetToken(Long userId) {
        cancelActiveTokens(userId, TokenType.RESET);

        ResetToken token = new ResetToken(userId, RESET_TOKEN_TTL);
        tokenRepository.save(token);
        return token.getRawToken();
    }

    /** Проверка и однократное использование атомарны; изменение пользователя участвует в той же транзакции. */
    @Transactional
    public Long consumeToken(String rawToken, TokenType expectedType) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Token is blank");
        }

        String hash = Token.hash(rawToken);
        Long userId = tokenRepository.findUserIdByTokenHash(hash)
                .orElseThrow(() -> new TokenNotFoundException("Invalid or expired token"));
        lockUser(userId);
        Token token = tokenRepository.findByTokenHashForUpdate(hash).orElseThrow(() ->
                new TokenNotFoundException("Invalid or expired token"));

        if (!token.getType().equals(expectedType)) {
            throw new InvalidTokenException("Invalid token type");
        }

        TokenDetails details = token.getDetails();

        if (details.isConsumed()) {
            throw new TokenAlreadyUsedException("Token has already been used");
        }

        if (!details.getExpiresAt().isAfter(Instant.now())) {
            throw new TokenExpiredException("Token has expired");
        }

        details.setConsumed(true);
        tokenRepository.save(token);
        return details.getUserId();
    }

    @Transactional
    public void cancelActiveTokens(Long userId, TokenType type) {
        cancelOtherActiveTokens(userId, type, null);
    }

    @Transactional
    public void cancelOtherActiveTokens(Long userId, TokenType type, String keepHash) {
        lockUser(userId);
        List<Token> tokens = tokenRepository.findActiveTokensByUserId(userId);
        for (Token token : tokens) {
            if (token.getType() == type && !token.getDetails().getTokenHash().equals(keepHash)) {
                token.getDetails().setConsumed(true);
                tokenRepository.save(token);
            }
        }
    }

    /** Новый почтовый токен не отменяет доставленную ссылку до успешной отправки нового письма. */
    @Transactional
    public Token prepareEmailToken(Long userId, TokenType type) {
        lockUser(userId);
        Token token = type == TokenType.VERIFY
                ? new VerifyToken(userId, VERIFY_TOKEN_TTL) : new ResetToken(userId, RESET_TOKEN_TTL);
        return tokenRepository.save(token);
    }

    private void lockUser(Long userId) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }
}
