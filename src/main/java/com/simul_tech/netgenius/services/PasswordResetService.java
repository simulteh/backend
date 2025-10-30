package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.*;
import com.simul_tech.netgenius.models.PasswordResetToken;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserService userService;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    public void createPasswordResetToken(String email) {
        try {
            User user = userService.findByEmail(email);
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new PasswordResetToken(token, user.getId());

            passwordResetTokenRepository.save(passwordResetToken);
            sendPasswordResetEmail(email, token);
            log.info("Password reset token created for user: {}", user.getEmail());
        } catch (EmailNotFound e) {
            log.info("User with email not found: {}", e.getMessage());
            throw e;
        }
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (passwordResetToken.isExpired()) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new ExpiredTokenException("Token has expired");
        }
        try {
            User user = userService.findById(passwordResetToken.getUserId());
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);

            passwordResetTokenRepository.delete(passwordResetToken);
            log.info("Password successfully reset for user: {}", user.getEmail());
        } catch (UserNotFoundException e) {
            log.info("User not found: {}", e.getMessage());
            throw e;
        }
    }

    private void sendPasswordResetEmail(String email, String token) {
        if (javaMailSender == null) {
            log.warn("JavaMailSender not configured, skipping email sending");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("abtserenov@edu.hse.ru");
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText("Чтобы сбросить пароль, перейдите по ссылке:\n\n" +
                    "http://simultech.ru/reset-password?token=" + token);
            javaMailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (EmailSenderException e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new EmailSenderException("Failed to send email");
        }
    }
}
