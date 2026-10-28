package com.simul_tech.netgenius;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import com.simul_tech.netgenius.models.LabAttempt;
import com.simul_tech.netgenius.models.MailOutbox;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

/** Same regression scenarios against the PostgreSQL service in CI; opt in locally through TEST_POSTGRES_URL. */
@EnabledIfEnvironmentVariable(named = "TEST_POSTGRES_URL", matches = "jdbc:postgresql:.*")
@SpringBootTest(properties = {
        "app.frontend-url=https://frontend.example.test", "app.teacher-request-email=review@example.test",
        "app.mail.from=support@example.test",
        "spring.datasource.url=${TEST_POSTGRES_URL}",
        "spring.datasource.username=${TEST_POSTGRES_USER:postgres}",
        "spring.datasource.password=${TEST_POSTGRES_PASSWORD:postgres}",
        "spring.datasource.driver-class-name=org.postgresql.Driver"
})
class BackendPostgresAuditTest extends BackendAuditFixTest {
    @Test
    void postgresUpdatePreservesLegacyAttemptsAndAddsOutboxAndNullableDedupKeys() throws Exception {
        String url = System.getenv("TEST_POSTGRES_URL");
        String username = System.getenv().getOrDefault("TEST_POSTGRES_USER", "postgres");
        String password = System.getenv().getOrDefault("TEST_POSTGRES_PASSWORD", "postgres");
        String schema = "audit_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            JdbcTemplate jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbc.execute("CREATE SCHEMA " + schema);
            try {
                connection.setSchema(schema);
                jdbc.execute("CREATE TABLE lab_attempts (attempt_id bigserial PRIMARY KEY, student_id bigint NOT NULL, lab_id bigint NOT NULL, status varchar(20) NOT NULL, score integer)");
                jdbc.update("INSERT INTO lab_attempts (student_id, lab_id, status, score) VALUES (1, 1, 'COMPLETED', 85), (1, 1, 'COMPLETED', 90)");
                Properties settings = new Properties();
                settings.setProperty("hibernate.connection.url", url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema);
                settings.setProperty("hibernate.connection.username", username);
                settings.setProperty("hibernate.connection.password", password);
                settings.setProperty("hibernate.default_schema", schema);
                settings.setProperty("hibernate.hbm2ddl.auto", "update");
                settings.setProperty("hibernate.hbm2ddl.halt_on_error", "true");
                for (int i = 0; i < 2; i++) {
                    try (SessionFactory ignored = new Configuration().addAnnotatedClass(LabAttempt.class)
                            .addAnnotatedClass(MailOutbox.class).setProperties(settings).buildSessionFactory()) {
                        assertThat(jdbc.queryForList("SELECT score FROM lab_attempts ORDER BY attempt_id", Integer.class)).containsExactly(85, 90);
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM lab_attempts WHERE event_key IS NULL AND payload_hash IS NULL", Integer.class)).isEqualTo(2);
                        assertThat(jdbc.queryForObject("SELECT count(*) FROM mail_outbox", Integer.class)).isZero();
                    }
                }
                jdbc.update("INSERT INTO lab_attempts (student_id, lab_id, status, event_key) VALUES (1, 1, 'COMPLETED', 'event-key')");
                assertThatThrownBy(() -> jdbc.update("INSERT INTO lab_attempts (student_id, lab_id, status, event_key) VALUES (1, 1, 'COMPLETED', 'event-key')"))
                        .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
            } finally {
                connection.setSchema("public");
                jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }
}
