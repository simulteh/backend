package com.simul_tech.netgenius;

import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.configurators.SwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;
import com.simul_tech.netgenius.services.EmailTemplateService;
import com.simul_tech.netgenius.services.MailSenderService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Проверяет реальный production-файл, а не одноимённый H2-файл из test/resources. */
class DeploymentConfigurationTest {
    @Test
    void databaseMailAndAdminSettingsUseEnvironment() {
        runner(Map.of()).run(context -> {
            assertThat(context).hasNotFailed();
            var environment = context.getEnvironment();
            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://postgres:5432/vm_database");
            assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("vm_user");
            assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("db-test-password");
            assertThat(environment.getProperty("app.jwt.secret")).isEqualTo("jwt-test-secret");
            assertThat(environment.getProperty("app.admin.email")).isEqualTo("admin@example.test");
            assertThat(environment.getProperty("app.admin.password")).isEqualTo("admin-test-password");
            assertThat(environment.getProperty("app.seed.labs")).isEqualTo("false");
            assertThat(environment.getProperty("app.website-url")).isEqualTo("https://frontend.example.test");
            assertThat(environment.getProperty("app.mail.from")).isEqualTo("mail@example.test");
            assertThat(environment.getProperty("app.teacher-request-email")).isEqualTo("review@example.test");
            assertThat(environment.getProperty("server.forward-headers-strategy")).isEqualTo("none");
            assertThat(context.getBean(OpenAPI.class).getServers()).isNull();

            JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
            assertThat(sender.getHost()).isEqualTo("smtp.example.test");
            assertThat(sender.getPort()).isEqualTo(587);
            assertThat(sender.getUsername()).isEqualTo("mail@example.test");
            assertThat(sender.getPassword()).isEqualTo("smtp-test-password");
            assertThat(sender.getJavaMailProperties())
                    .containsEntry("mail.smtp.starttls.enable", "true")
                    .containsEntry("mail.smtp.starttls.required", "true")
                    .containsEntry("mail.smtp.ssl.enable", "false");
        });
    }

    @Test
    void smtp465SupportsImplicitTlsAndCustomSender() {
        runner(Map.of("SMTP_PORT", "465", "SMTP_SSL", "true", "SMTP_STARTTLS", "false",
                "SMTP_FROM", "support@example.test", "PUBLIC_API_URL", "/api",
                "FORWARD_HEADERS_STRATEGY", "framework")).run(context -> {
            assertThat(context).hasNotFailed();
            JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
            assertThat(sender.getPort()).isEqualTo(465);
            assertThat(context.getEnvironment().getProperty("app.mail.from")).isEqualTo("support@example.test");
            assertThat(context.getEnvironment().getProperty("server.forward-headers-strategy")).isEqualTo("framework");
            assertThat(context.getBean(OpenAPI.class).getServers().get(0).getUrl()).isEqualTo("/api");
            assertThat(sender.getJavaMailProperties())
                    .containsEntry("mail.smtp.ssl.enable", "true")
                    .containsEntry("mail.smtp.starttls.enable", "false")
                    .containsEntry("mail.smtp.starttls.required", "false");
        });
    }

    @Test
    void missingJwtSecretPreventsStartup() {
        runner(Map.of("JWT_SECRET", "${MISSING_DEPLOYMENT_JWT_SECRET}"))
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void existingRootComposeDatasourceSettingsKeepPrecedence() {
        runner(Map.of("SPRING_DATASOURCE_URL", "jdbc:postgresql://database:5432/netgenius",
                "SPRING_DATASOURCE_USERNAME", "postgres", "SPRING_DATASOURCE_PASSWORD", "existing-test-password",
                "SPRING_PROFILES_ACTIVE", "prod")).run(context -> {
            assertThat(context).hasNotFailed();
            var environment = context.getEnvironment();
            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://database:5432/netgenius");
            assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("postgres");
            assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("existing-test-password");
        });
    }

    @Test
    void emptySenderFromEnvFileUsesSmtpUsername() {
        JavaMailSender mail = mock(JavaMailSender.class);
        when(mail.createMimeMessage()).thenAnswer(invocation -> new MimeMessage((Session) null));
        runner(Map.of("SMTP_FROM", ""))
                .withBean(JavaMailSender.class, () -> mail)
                .withBean(EmailTemplateService.class)
                .withBean(MailSenderService.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    context.getBean(MailSenderService.class)
                            .sendCreationEmail("student@example.test", "test-token", "Student");
                    ArgumentCaptor<MimeMessage> message = ArgumentCaptor.forClass(MimeMessage.class);
                    verify(mail).send(message.capture());
                    assertThat(message.getValue().getFrom()[0].toString()).contains("mail@example.test");
                });
    }

    private ApplicationContextRunner runner(Map<String, String> overrides) {
        Map<String, Object> variables = new HashMap<>(Map.ofEntries(
                Map.entry("POSTGRES_DB", "vm_database"),
                Map.entry("POSTGRES_USER", "vm_user"),
                Map.entry("POSTGRES_PASSWORD", "db-test-password"),
                Map.entry("JWT_SECRET", "jwt-test-secret"),
                Map.entry("ADMIN_EMAIL", "admin@example.test"),
                Map.entry("ADMIN_PASSWORD", "admin-test-password"),
                Map.entry("SMTP_HOST", "smtp.example.test"),
                Map.entry("SMTP_USERNAME", "mail@example.test"),
                Map.entry("SMTP_PASSWORD", "smtp-test-password"),
                Map.entry("FRONTEND_URL", "https://frontend.example.test"),
                Map.entry("TEACHER_REQUEST_EMAIL", "review@example.test")));
        variables.putAll(overrides);
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        PropertyPlaceholderAutoConfiguration.class, MailSenderAutoConfiguration.class))
                .withBean(JwtCore.class)
                .withUserConfiguration(SwaggerConfig.class)
                .withInitializer(context -> {
                    try {
                        context.getEnvironment().getPropertySources().addFirst(new ResourcePropertySource(
                                "production-config", new FileSystemResource("src/main/resources/application.properties")));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    context.getEnvironment().getPropertySources().addFirst(
                            new SystemEnvironmentPropertySource("deployment-environment", variables));
                });
    }
}
