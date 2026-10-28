package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.EmailSenderException;
import com.simul_tech.netgenius.models.Student;
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
    private String verificationRequestEmail;

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

    public void sendStudentRequest(User user, Long requestId, Student student) {
        requireVerificationRecipient();
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(senderAddress(), "СИМУЛТЕХ");
            helper.setTo(verificationRequestEmail);
            helper.setReplyTo(user.getEmail());
            helper.setSubject("Заявка на статус студента — СИМУЛТЕХ");

            StringBuilder body = new StringBuilder(verificationHeader(user, requestId));
            appendDetail(body, "ФИО", student.getFullName());
            appendDetail(body, "Год рождения", student.getBirthYear());
            appendDetail(body, "Телефон", student.getPhone());
            appendDetail(body, "ID вуза", student.getUniversityId());
            appendDetail(body, "Факультет", student.getFaculty());
            appendDetail(body, "Специальность", student.getSpeciality());
            appendDetail(body, "Направление", student.getDirection());
            appendDetail(body, "Курс", student.getCourse());
            appendDetail(body, "Степень", student.getDegree());
            body.append("\n\nРоль STUDENT выдаётся администратором после проверки заявки.")
                    .append(reviewInstructions(requestId));
            helper.setText(body.toString(), false);
            send(message);
        } catch (EmailSenderException e) {
            throw e;
        } catch (Exception e) {
            throw new EmailSenderException("Не удалось подготовить заявку студента");
        }
    }

    public void sendTeacherRequest(User user, Long requestId, String fullName, String organization,
                                   String filename, String contentType, byte[] contents) {
        requireVerificationRecipient();
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderAddress(), "СИМУЛТЕХ");
            helper.setTo(verificationRequestEmail);
            helper.setReplyTo(user.getEmail());
            helper.setSubject("Заявка на статус преподавателя — СИМУЛТЕХ");
            helper.setText(verificationHeader(user, requestId) + "\nФИО: " + fullName
                    + "\nОрганизация: " + organization
                    + "\n\nРоль TEACHER выдаётся администратором после проверки документа."
                    + reviewInstructions(requestId), false);
            helper.addAttachment(filename, new ByteArrayResource(contents), contentType);
            send(message);
        } catch (EmailSenderException e) {
            throw e;
        } catch (Exception e) {
            throw new EmailSenderException("Не удалось подготовить заявку преподавателя");
        }
    }

    private void requireVerificationRecipient() {
        if (verificationRequestEmail == null || verificationRequestEmail.isBlank()) {
            throw new EmailSenderException("Не настроен адрес получения заявок на подтверждение роли");
        }
    }

    private String verificationHeader(User user, Long requestId) {
        return "Заявка № " + requestId + "\nID пользователя: " + user.getId()
                + "\nEmail: " + user.getEmail();
    }

    private String reviewInstructions(Long requestId) {
        return "\n\nОдобрение в Swagger: POST /admin/verifications/" + requestId + "/approve"
                + "\nВ поле id укажите ID заявки: " + requestId + ". ID пользователя сюда не передаётся.";
    }

    private void appendDetail(StringBuilder body, String label, Object value) {
        if (value != null && !value.toString().isBlank()) {
            body.append('\n').append(label).append(": ").append(value);
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
