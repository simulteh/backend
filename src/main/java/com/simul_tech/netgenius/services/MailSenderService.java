package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.EmailSenderException;
import com.simul_tech.netgenius.models.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender javaMailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.frontend-url:https://simulteh.com}")
    private String frontendUrl;

    @Value("${app.website-url:${app.frontend-url:https://simulteh.com}}")
    private String websiteUrl;

    @Value("${app.teacher-request-email:}")
    private String teacherRequestEmail;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String from;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    public void sendPasswordResetEmail(String email, String token, String userName) {
        String resetLink = frontendLink("/reset-password", token);
        String subject = "Сброс пароля — СИМУЛТЕХ";

        MimeMessage message = buildMessage(subject, email,
                emailTemplateService.buildPasswordResetEmail(resetLink, userName));

        send(message);
        log.info("Письмо для сброса пароля отправлено на: {}", email);
    }

    public void sendCreationEmail(String email, String token, String userName) {
        String creationLink = frontendLink("/confirm-email", token);
        String subject = "Подтверждение регистрации — СИМУЛТЕХ";

        MimeMessage message = buildMessage(subject, email,
                emailTemplateService.buildVerificationEmail(creationLink, userName, websiteUrl));

        send(message);
        log.info("Письмо для подтверждения регистрации отправлено на: {}", email);

    }

    public void sendTeacherRequest(User user, Long requestId, String fullName, String organization,
                                   String filename, String contentType, byte[] contents) {
        if (teacherRequestEmail == null || teacherRequestEmail.isBlank()) {
            throw new EmailSenderException("Не настроен адрес получения заявок преподавателей");
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderAddress(), "СИМУЛТЕХ");
            helper.setTo(teacherRequestEmail);
            helper.setReplyTo(user.getEmail());
            helper.setSubject("Заявка на статус преподавателя — СИМУЛТЕХ");
            helper.setText("Заявка № " + requestId + "\nID пользователя: " + user.getId()
                    + "\nEmail: " + user.getEmail() + "\nФИО: " + fullName
                    + "\nОрганизация: " + organization
                    + "\n\nРоль TEACHER выдаётся администратором после проверки документа.", false);
            helper.addAttachment(filename, new ByteArrayResource(contents), contentType);
            send(message);
        } catch (EmailSenderException e) {
            throw e;
        } catch (Exception e) {
            throw new EmailSenderException("Не удалось подготовить заявку преподавателя");
        }
    }

    private String frontendLink(String path, String token) {
        return UriComponentsBuilder.fromUriString(frontendUrl.replaceAll("/+$", ""))
                .path(path).queryParam("token", token).build().encode().toUriString();
    }

    private void send(MimeMessage message) {
        try {
            javaMailSender.send(message);
        } catch (org.springframework.mail.MailException e) {
            throw new EmailSenderException("Не удалось отправить письмо");
        }
    }

    private String senderAddress() {
        return from == null || from.isBlank() ? smtpUsername : from;
    }

    private MimeMessage buildMessage(String subject, String email, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject(subject);
            helper.setFrom(senderAddress(), "СИМУЛТЕХ");
            helper.setTo(email);

            helper.setText(htmlContent, true);

            return message;
        } catch (Exception e) {
            log.error("Ошибка отправки письма: {}", e.getMessage());
            throw new EmailSenderException("Failed to send email");
        }
    }
}
