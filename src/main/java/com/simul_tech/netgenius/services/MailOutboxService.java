package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.impls.TokenType;
import com.simul_tech.netgenius.models.*;
import com.simul_tech.netgenius.repositories.*;
import com.simul_tech.netgenius.security.MailTokenCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailOutboxService {
    public record Queued(Long id) { }
    private final MailOutboxRepository outbox;
    private final UserRepository users;
    private final TokenRepository tokens;
    private final TokenService tokenService;
    private final MailSenderService mail;
    private final MailTokenCipher cipher;
    private final ApplicationEventPublisher events;

    @Transactional
    public void enqueue(Long userId, String recipient, TokenType type) {
        User user = users.findByIdForUpdate(userId).orElseThrow();
        if (!user.getEmail().equals(recipient) || (type == TokenType.VERIFY && user.isEnabled())) return;
        for (MailOutbox previous : outbox.findByUserIdAndTypeAndStatus(userId, type, MailOutbox.Status.PENDING)) {
            tokens.findByTokenHashForUpdate(previous.getTokenHash())
                    .ifPresent(token -> token.getDetails().setConsumed(true));
            finish(previous, MailOutbox.Status.CANCELLED);
        }
        Token token = tokenService.prepareEmailToken(userId, type);
        String rawToken = token instanceof VerifyToken verify ? verify.getRawToken() : ((ResetToken) token).getRawToken();
        MailOutbox entry = new MailOutbox();
        entry.setUserId(userId);
        entry.setRecipient(recipient);
        entry.setType(type);
        entry.setTokenHash(token.getDetails().getTokenHash());
        entry.setCreatedAt(Instant.now());
        entry.setExpiresAt(token.getDetails().getExpiresAt());
        entry.setNextAttemptAt(Instant.now());
        entry.setEncryptedToken(cipher.encrypt(rawToken, entry.encryptionContext()));
        outbox.save(entry);
        events.publishEvent(new Queued(entry.getId()));
    }

    /** Каждый отправитель блокирует владельца, затем запись очереди; повторный воркер увидит SENT. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliver(Long id) {
        Long userId = outbox.findUserIdById(id).orElse(null);
        if (userId == null) return;
        User user = users.findByIdForUpdate(userId).orElse(null);
        MailOutbox entry = outbox.findByIdForUpdate(id).orElse(null);
        if (entry == null || entry.getStatus() != MailOutbox.Status.PENDING) return;
        Instant now = Instant.now();
        Token token = tokens.findByTokenHashForUpdate(entry.getTokenHash()).orElse(null);
        if (user == null || !entry.getRecipient().equals(user.getEmail())
                || (entry.getType() == TokenType.VERIFY && user.isEnabled())
                || token == null || token.getDetails().isConsumed() || !entry.getExpiresAt().isAfter(now)
                || !token.getDetails().getExpiresAt().isAfter(now)) {
            if (token != null) token.getDetails().setConsumed(true);
            finish(entry, MailOutbox.Status.CANCELLED);
            return;
        }
        if (entry.getNextAttemptAt().isAfter(now)) return;
        entry.setAttempts(entry.getAttempts() + 1);
        try {
            String rawToken = cipher.decrypt(entry.getEncryptedToken(), entry.encryptionContext());
            if (entry.getType() == TokenType.VERIFY) {
                mail.sendCreationEmail(entry.getRecipient(), rawToken, user.getFirstName());
            } else {
                mail.sendPasswordResetEmail(entry.getRecipient(), rawToken, user.getFirstName());
            }
        } catch (RuntimeException e) {
            long delay = Math.min(900L, 60L << Math.min(entry.getAttempts() - 1, 4));
            entry.setNextAttemptAt(now.plusSeconds(delay));
            log.warn("Mail outbox {} attempt {} failed; retry scheduled ({})", id, entry.getAttempts(), e.getClass().getSimpleName());
            return;
        }
        tokenService.cancelOtherActiveTokens(userId, entry.getType(), entry.getTokenHash());
        entry.setSentAt(Instant.now());
        finish(entry, MailOutbox.Status.SENT);
    }

    private void finish(MailOutbox entry, MailOutbox.Status status) {
        entry.setStatus(status);
        entry.setEncryptedToken(null);
    }
}
