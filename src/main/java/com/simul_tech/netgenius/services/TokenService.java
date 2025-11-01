package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.*;
import com.simul_tech.netgenius.impls.TokenType;
import com.simul_tech.netgenius.models.ResetToken;
import com.simul_tech.netgenius.models.Token;
import com.simul_tech.netgenius.models.VerifyToken;
import com.simul_tech.netgenius.impls.TokenDetails;
import com.simul_tech.netgenius.repositories.TokenRepository;
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

    @Transactional(readOnly = true)
    public Long validateToken(String rawToken, TokenType expectedType) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Token is blank");
        }

        String hash = Token.hash(rawToken);
        Token token = tokenRepository.findByTokenHash(hash).orElseThrow(() ->
                new TokenNotFoundException("Invalid or expired token"));

        if (!token.getType().equals(expectedType)) {
            throw new InvalidTokenException("Invalid token type");
        }

        TokenDetails details = token.getDetails();

        if (details.isConsumed()) {
            throw new TokenAlreadyUsedException("Token has already been used");
        }

        if (details.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Token has expired");
        }

        return details.getUserId();
    }

    @Transactional
    public void consumeToken(String rawToken) {
        String hash = Token.hash(rawToken);
        Token token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        token.getDetails().setConsumed(true);
        tokenRepository.save(token);
    }

    @Transactional
    public void cancelActiveTokens(Long userId, TokenType type) {
        List<Token> tokens = tokenRepository.findActiveTokensByUserId(userId);
        for (Token token : tokens) {
            if (token.getType() == type) {
                token.getDetails().setConsumed(true);
                tokenRepository.save(token);
                break;
            }
        }
    }
}
