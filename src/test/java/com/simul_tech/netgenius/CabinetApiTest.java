package com.simul_tech.netgenius;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.*;
import com.simul_tech.netgenius.repositories.*;
import com.simul_tech.netgenius.security.JwtCore;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.frontend-url=https://frontend.example.test/", "app.teacher-request-email=review@example.test",
        "app.mail.from=support@example.test"
})
@AutoConfigureMockMvc
class CabinetApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository users;
    @Autowired private TeacherRepository teachers;
    @Autowired private StudentRepository students;
    @Autowired private GroupRepository groups;
    @Autowired private StudentGroupRepository memberships;
    @Autowired private UniversityRepository universities;
    @Autowired private UserLevelScoreRepository scores;
    @Autowired private VerificationRequestRepository verifications;
    @Autowired private JwtCore jwtCore;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockitoBean private JavaMailSender mail;

    @BeforeEach
    void prepareMail() {
        when(mail.createMimeMessage()).thenAnswer(ignored -> new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    void profileReturnsPublicUserIdAndActualRoleWithoutLosingExistingFields() throws Exception {
        User student = user(Role.STUDENT);
        JsonNode profile = read(mvc.perform(auth(get("/profile/me"), student))
                .andExpect(status().isOk()).andReturn());
        assertThat(profile.path("id").asLong()).isEqualTo(student.getId());
        assertThat(profile.path("role").asText()).isEqualTo("STUDENT");
        assertThat(profile.path("firstName").asText()).isEqualTo(student.getFirstName());
        assertThat(profile.path("email").asText()).isEqualTo(student.getEmail());
        assertThat(profile.has("avatarUrl")).isTrue();
        assertThat(profile.get("avatarUrl").isNull()).isTrue();
        mvc.perform(auth(get("/profile/me"), user(Role.USER)))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/profile/me", "/profile/scores", "/groups", "/groups/my"})
    void protectedReadsRequireToken(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void scoresHaveElevenPositionsKeepMaximumAndSeparateUsers() throws Exception {
        User student = user(Role.STUDENT);
        assertThat(read(mvc.perform(auth(get("/profile/scores"), student)).andReturn()).size()).isEqualTo(11);
        assertThat(scoreVector(student)).containsOnlyNulls();
        saveScore(student, 0, 0);
        saveScore(student, 1, 9);
        saveScore(student, 1, 3);
        saveScore(student, 10, 10);
        assertThat(scoreVector(student)).containsExactly(0, 9, null, null, null, null, null, null, null, null, 10);
        assertThat(scoreVector(user(Role.STUDENT))).containsOnlyNulls();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"level\":null,\"score\":1}", "{\"level\":1,\"score\":null}",
            "{\"level\":-1,\"score\":1}", "{\"level\":11,\"score\":1}",
            "{\"level\":1,\"score\":-1}", "{\"level\":1,\"score\":11}",
            "{\"level\":1.5,\"score\":1}", "{\"level\":1,\"score\":2.5}"
    })
    void scoresRejectMissingFractionalAndOutOfRangeValues(String body) throws Exception {
        User student = user(Role.STUDENT);
        mvc.perform(auth(post("/profile/scores"), student).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        assertThat(scores.findByUserIdIn(List.of(student.getId()))).isEmpty();
    }

    @Test
    void teacherManagesGroupsUsingPublicStudentIdAndSeesSameScores() throws Exception {
        User teacher = user(Role.TEACHER);
        User student = user(Role.STUDENT);
        saveScore(student, 3, 8);
        JsonNode created = createGroup(teacher, "Группа 101", "Юридическое название вуза");
        long id = created.path("id").asLong();
        assertThat(created.path("studentsCount").asInt()).isZero();
        for (int i = 0; i < 2; i++) {
            mvc.perform(auth(post("/groups/" + id + "/students"), teacher).contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(Map.of("studentId", student.getId()))))
                    .andExpect(status().isOk());
        }
        assertThat(students.findByUserId(student.getId()).orElseThrow().getId()).isNotEqualTo(student.getId());
        JsonNode detail = groupDetail(teacher, id);
        assertThat(detail.path("students").size()).isEqualTo(1);
        assertThat(detail.path("students").get(0).path("id").asLong()).isEqualTo(student.getId());
        assertThat(detail.path("students").get(0).path("scores").get(3).asInt()).isEqualTo(8);
        assertThat(detail.path("students").get(0).path("scores").size()).isEqualTo(11);
        String invite = detail.path("inviteCode").asText();
        assertThat(invite).hasSizeGreaterThanOrEqualTo(24);
        assertThat(groupDetail(teacher, id).path("inviteCode").asText()).isEqualTo(invite);
        mvc.perform(auth(get("/groups"), teacher))
                .andExpect(jsonPath("$[0].name").value("Группа 101"))
                .andExpect(jsonPath("$[0].studentsCount").value(1));
        mvc.perform(auth(put("/groups/" + id), teacher).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Группа 102\",\"organization\":\"Другой вуз\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.organization").value("Другой вуз"));
        mvc.perform(auth(delete("/groups/" + id + "/students/" + student.getId()), teacher))
                .andExpect(status().isOk());
        assertThat(groupDetail(teacher, id).path("students").size()).isZero();
        assertThat(scoreVector(student).get(3)).isEqualTo(8);
    }

    @Test
    void studentJoinsByInviteAndSeesTeacherNameWithoutDuplicateMemberships() throws Exception {
        User teacher = user(Role.TEACHER);
        User student = user(Role.STUDENT);
        long id = createGroup(teacher, "G", "Вуз").path("id").asLong();
        String code = groupDetail(teacher, id).path("inviteCode").asText();
        mvc.perform(auth(get("/groups/my"), student)).andExpect(content().json("[]"));
        for (int i = 0; i < 2; i++) {
            mvc.perform(auth(post("/groups/join/" + code), student))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        }
        mvc.perform(auth(get("/groups/my"), student)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].teacher.lastName").value(teacher.getLastName()))
                .andExpect(jsonPath("$[0].teacher.firstName").value(teacher.getFirstName()))
                .andExpect(jsonPath("$[0].teacher.middleName").value(teacher.getMiddleName()));
        mvc.perform(auth(post("/groups/join/does-not-exist"), student)).andExpect(status().isNotFound());
    }

    @Test
    void rolesAndGroupOwnershipProtectEveryTeacherOperation() throws Exception {
        User teacher = user(Role.TEACHER);
        User other = user(Role.TEACHER);
        User student = user(Role.STUDENT);
        long id = createGroup(teacher, "G", "Вуз").path("id").asLong();
        mvc.perform(auth(get("/groups/" + id), student)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/groups"), user(Role.USER))).andExpect(status().isForbidden());
        mvc.perform(auth(get("/groups/" + id), other)).andExpect(status().isNotFound());
        mvc.perform(auth(put("/groups/" + id), other).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"stolen\",\"organization\":\"org\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(auth(post("/groups/" + id + "/students"), other).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("studentId", student.getId()))))
                .andExpect(status().isNotFound());
        mvc.perform(auth(delete("/groups/" + id + "/students/" + student.getId()), other))
                .andExpect(status().isNotFound());
        mvc.perform(auth(get("/groups/my"), teacher)).andExpect(status().isForbidden());
        mvc.perform(auth(post("/groups/join/" + groupDetail(teacher, id).path("inviteCode").asText()), teacher))
                .andExpect(status().isForbidden());
        assertThat(groupDetail(teacher, id).path("students").size()).isZero();
    }

    @Test
    void teacherCannotAddPlainUserAndInvalidGroupInputIsRejected() throws Exception {
        User teacher = user(Role.TEACHER);
        long id = createGroup(teacher, "G", "Вуз").path("id").asLong();
        mvc.perform(auth(post("/groups/" + id + "/students"), teacher).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("studentId", user(Role.USER).getId()))))
                .andExpect(status().isConflict());
        mvc.perform(auth(post("/groups/" + id + "/students"), teacher).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(auth(post("/groups"), teacher).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"organization\":\"Вуз\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void existingLmsGroupAppearsWithUniversityNameAndGetsStableInvite() throws Exception {
        User teacher = user(Role.TEACHER);
        createGroup(teacher, "new", "org");
        University university = new University();
        university.setName("Вуз из справочника");
        universities.save(university);
        Group group = new Group();
        group.setTeacherId(teachers.findByUserId(teacher.getId()).orElseThrow().getId());
        group.setGroupName("Legacy");
        group.setUniversityId(university.getId());
        groups.saveAndFlush(group);
        group.setInviteCode(null);
        groups.saveAndFlush(group);
        JsonNode detail = groupDetail(teacher, group.getId());
        assertThat(detail.path("organization").asText()).isEqualTo(university.getName());
        assertThat(detail.path("inviteCode").asText()).isNotBlank();
        assertThat(groupDetail(teacher, group.getId()).path("inviteCode")).isEqualTo(detail.path("inviteCode"));
    }

    @Test
    void teacherRequestSendsDocumentAndNeedsManualApproval() throws Exception {
        User candidate = user(Role.USER);
        mvc.perform(teacherRequest(candidate, pdf())).andExpect(status().isOk());
        var request = verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId()).get(0);
        assertThat(request.getStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(users.findById(candidate.getId()).orElseThrow().getRole()).isEqualTo(Role.USER);
        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail).send(captor.capture());
        MimeMessage message = captor.getValue();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("review@example.test");
        assertThat(message.getReplyTo()[0].toString()).isEqualTo(candidate.getEmail());
        assertThat(text(message)).contains("Фамилия Имя Отчество", "Юридическое название вуза", candidate.getId().toString());
        assertThat(attachmentNames(message)).contains("employment.pdf");
        mvc.perform(teacherRequest(candidate, pdf())).andExpect(status().isConflict());
        User admin = users.findByEmail("admin@netgenius.ru").orElseThrow();
        mvc.perform(auth(post("/admin/verifications/" + request.getId() + "/approve"), admin))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/profile/me"), candidate)).andExpect(jsonPath("$.role").value("TEACHER"));
        createGroup(users.findById(candidate.getId()).orElseThrow(), "После проверки", "Вуз");
    }

    @Test
    void studentRequestEmailsAdministratorAndNeedsManualApproval() throws Exception {
        User candidate = user(Role.USER);
        JsonNode request = read(mvc.perform(auth(post("/verification/student"), candidate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "Тестов Иван Иванович", "birthYear", 2003,
                                "phone", "+79990000000", "faculty", "ФКН",
                                "speciality", "Программная инженерия", "direction", "09.03.04",
                                "course", 2, "degree", "Бакалавриат"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestType").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("PENDING")).andReturn());

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail).send(sent.capture());
        MimeMessage message = sent.getValue();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("review@example.test");
        assertThat(message.getReplyTo()[0].toString()).isEqualTo(candidate.getEmail());
        assertThat(message.getSubject()).isEqualTo("Заявка на статус студента — СИМУЛТЕХ");
        assertThat(attachmentNames(message)).isEmpty();
        assertThat(text(message)).contains(
                "Заявка № " + request.path("id").asLong(),
                "ID пользователя: " + candidate.getId(), candidate.getEmail(),
                "Тестов Иван Иванович", "Год рождения: 2003", "Телефон: +79990000000",
                "Факультет: ФКН", "Специальность: Программная инженерия",
                "Направление: 09.03.04", "Курс: 2", "Степень: Бакалавриат",
                "/admin/verifications/" + request.path("id").asLong() + "/approve");
        assertThat(text(message)).doesNotContain("null");
        assertThat(students.findByUserId(candidate.getId()).orElseThrow().isVerified()).isFalse();
        assertThat(users.findById(candidate.getId()).orElseThrow().getRole()).isEqualTo(Role.USER);
        mvc.perform(auth(get("/verification/status"), candidate))
                .andExpect(jsonPath("$[0].id").value(request.path("id").asLong()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        User admin = users.findByEmail("admin@netgenius.ru").orElseThrow();
        mvc.perform(auth(post("/admin/verifications/" + request.path("id").asLong() + "/approve"), admin))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/profile/me"), candidate)).andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void studentMailFailureRollsBackRequestAndAllowsRetry() throws Exception {
        User candidate = user(Role.USER);
        doThrow(new MailSendException("SMTP unavailable")).when(mail).send(any(MimeMessage.class));
        mvc.perform(auth(post("/verification/student"), candidate)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Тестов Иван\"}"))
                .andExpect(status().isServiceUnavailable());
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).isEmpty();
        assertThat(students.findByUserId(candidate.getId())).isEmpty();
        assertThat(users.findById(candidate.getId()).orElseThrow().getRole()).isEqualTo(Role.USER);

        doNothing().when(mail).send(any(MimeMessage.class));
        mvc.perform(auth(post("/verification/student"), candidate)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Тестов Иван\"}"))
                .andExpect(status().isCreated());
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).hasSize(1);
        assertThat(students.findByUserId(candidate.getId()).orElseThrow().isVerified()).isFalse();
    }

    @Test
    void duplicatePendingStudentRequestDoesNotSendAnotherEmail() throws Exception {
        User candidate = user(Role.USER);
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(auth(post("/verification/student"), candidate)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Тестов Иван\"}"))
                    .andExpect(status().is(attempt == 0 ? 201 : 409));
        }
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).hasSize(1);
        verify(mail, times(1)).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"student", "teacher"})
    void roleVerificationStillWorksWithoutDocumentUrls(String type) throws Exception {
        User candidate = user(Role.USER);
        Map<String, String> body = type.equals("teacher")
                ? Map.of("fullName", "Фамилия Имя Отчество", "workplace", "Вуз")
                : Map.of("fullName", "Фамилия Имя Отчество");
        JsonNode request = read(mvc.perform(auth(post("/verification/" + type), candidate)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING")).andReturn());
        mvc.perform(auth(get("/verification/status"), candidate))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(request.path("id").asLong()));
        mvc.perform(auth(get("/profile/me"), candidate)).andExpect(jsonPath("$.role").value("USER"));
        User admin = users.findByEmail("admin@netgenius.ru").orElseThrow();
        mvc.perform(auth(post("/admin/verifications/" + request.path("id").asLong() + "/approve"), admin))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/profile/me"), candidate))
                .andExpect(jsonPath("$.role").value(type.toUpperCase(Locale.ROOT)));
    }

    @Test
    void mailFailureRollsBackRequestAndAllowsRetry() throws Exception {
        User candidate = user(Role.USER);
        doThrow(new MailSendException("SMTP unavailable")).when(mail).send(any(MimeMessage.class));
        mvc.perform(teacherRequest(candidate, pdf())).andExpect(status().isServiceUnavailable());
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).isEmpty();
        assertThat(teachers.findByUserId(candidate.getId())).isEmpty();
        doNothing().when(mail).send(any(MimeMessage.class));
        mvc.perform(teacherRequest(candidate, pdf())).andExpect(status().isOk());
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"executable.exe", "document.docx", "fake.pdf", "image.png", ""})
    void invalidAttachmentsAreRejectedBeforeMail(String filename) throws Exception {
        User candidate = user(Role.USER);
        mvc.perform(teacherRequest(candidate, new MockMultipartFile("file", filename, "application/octet-stream", "not a document".getBytes())))
                .andExpect(status().isBadRequest());
        verify(mail, never()).send(any(MimeMessage.class));
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).isEmpty();
    }

    @Test
    void emptyAndOversizedFilesAreRejected() throws Exception {
        User candidate = user(Role.USER);
        mvc.perform(teacherRequest(candidate, new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0])))
                .andExpect(status().isBadRequest());
        mvc.perform(teacherRequest(candidate, new MockMultipartFile("file", "big.pdf", "application/pdf", new byte[5 * 1024 * 1024 + 1])))
                .andExpect(status().isBadRequest());
        verify(mail, never()).send(any(MimeMessage.class));
    }

    @Test
    void teacherRequestRequiresAuthenticationAndConfirmedEmailAndAllFields() throws Exception {
        mvc.perform(multipart("/profile/teacher-request").file(pdf()).param("fullName", "ФИО").param("organization", "Вуз"))
                .andExpect(status().isUnauthorized());
        User candidate = user(Role.USER);
        mvc.perform(auth(multipart("/profile/teacher-request").param("fullName", "ФИО").param("organization", "Вуз"), candidate))
                .andExpect(status().isBadRequest());
        mvc.perform(auth(multipart("/profile/teacher-request").file(pdf()).param("fullName", " ").param("organization", "Вуз"), candidate))
                .andExpect(status().isBadRequest());
        candidate.setEnabled(false);
        users.save(candidate);
        mvc.perform(teacherRequest(candidate, pdf())).andExpect(status().isForbidden());
        verify(mail, never()).send(any(MimeMessage.class));
    }

    @Test
    void confirmationAndResetLinksLeadToConfiguredFrontendAndResendDoesNotRevealUnknownEmail() throws Exception {
        User candidate = user(Role.USER);
        candidate.setEnabled(false);
        users.save(candidate);
        String known = mapper.writeValueAsString(Map.of("email", candidate.getEmail()));
        JsonNode resend = read(mvc.perform(post("/auth/resend-confirmation").contentType(MediaType.APPLICATION_JSON).content(known))
                .andExpect(status().isOk()).andReturn());
        JsonNode unknown = read(mvc.perform(post("/auth/resend-confirmation").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@example.test\"}"))
                .andExpect(status().isOk()).andReturn());
        assertThat(unknown).isEqualTo(resend);
        mvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(known))
                .andExpect(status().isOk());
        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail, times(2)).send(captor.capture());
        assertThat(text(captor.getAllValues().get(0))).contains("https://frontend.example.test/confirm-email?token=");
        assertThat(text(captor.getAllValues().get(1))).contains("https://frontend.example.test/reset-password?token=");
    }

    @Test
    void swaggerDescribesSuccessResponsesSchemasAndAuthentication() throws Exception {
        JsonNode api = read(mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn());
        api.path("paths").properties().forEach(path -> path.getValue().properties().forEach(method -> {
            JsonNode documented = method.getValue().path("responses");
            Iterable<String> responseCodes = documented::fieldNames;
            assertThat(responseCodes).as("Swagger must document success for %s %s", method.getKey(), path.getKey())
                    .anyMatch(code -> code.startsWith("2"));
        }));
        for (String path : List.of("/auth/register", "/groups", "/verification/student", "/verification/teacher",
                "/teacher/groups", "/teacher/groups/{id}/invite", "/teacher/university-request",
                "/admin/universities", "/admin/licenses/create", "/admin/licenses/batch")) {
            JsonNode created = api.path("paths").path(path).path("post").path("responses");
            assertThat(created.has("201")).as("POST %s creates a resource", path).isTrue();
            assertThat(created.has("200")).as("POST %s must not advertise 200", path).isFalse();
            assertThat(created.path("201").path("content").isEmpty()).isFalse();
        }
        JsonNode deleted = api.path("paths").path("/admin/universities/{id}").path("delete").path("responses");
        assertThat(deleted.has("204")).isTrue();
        assertThat(deleted.has("200")).isFalse();
        assertThat(deleted.path("204").has("content")).isFalse();
        for (String path : List.of("/groups", "/groups/my")) {
            JsonNode schema = api.path("paths").path(path).path("get").path("responses").path("200")
                    .path("content").path("application/json").path("schema");
            assertThat(schema.path("type").asText()).isEqualTo("array");
            assertThat(schema.path("items").path("$ref").asText()).startsWith("#/components/schemas/Cabinet");
        }
        assertThat(api.path("paths").path("/groups/{id}").path("get").path("responses").path("200")
                .path("content").path("application/json").path("schema").path("$ref").asText())
                .isEqualTo("#/components/schemas/CabinetGroupDetail");
        assertThat(api.path("security").isMissingNode()).isTrue();
        for (String path : List.of("/auth/login", "/auth/register", "/auth/refresh")) {
            assertThat(api.path("paths").path(path).path("post").path("security").isEmpty()).isTrue();
        }
        for (String path : List.of("/groups", "/auth/me", "/users/me", "/admin/licenses")) {
            assertThat(api.path("paths").path(path).path("get").path("security").get(0).has("bearerAuth"))
                    .as("GET %s requires a Bearer token", path).isTrue();
        }
        assertThat(api.path("paths").path("/auth/login").path("post").path("responses").has("401")).isTrue();
        assertThat(api.path("paths").path("/auth/refresh").path("post").path("responses").has("400")).isTrue();
        JsonNode responses = api.path("paths").path("/profile/me").path("get").path("responses");
        assertThat(responses.has("401")).isTrue();
        assertThat(responses.has("400")).isFalse();
        assertThat(responses.path("200").path("content").path("application/json").path("schema").path("$ref").asText())
                .isEqualTo("#/components/schemas/ProfileResponse");
        JsonNode profile = api.path("components").path("schemas").path("ProfileResponse").path("properties");
        assertThat(profile.has("id")).isTrue();
        assertThat(profile.has("role")).isTrue();
        assertThat(api.path("paths").path("/profile/teacher-request").path("post").path("requestBody")
                .path("content").has("multipart/form-data")).isTrue();
    }

    @Test
    void frontendCanPreflightPutAndDeleteWithAuthorizationHeader() throws Exception {
        for (String method : List.of("PUT", "DELETE")) {
            mvc.perform(options("/groups/1").header("Origin", "https://frontend.example.test")
                            .header("Access-Control-Request-Method", method)
                            .header("Access-Control-Request-Headers", "authorization,content-type"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void groupSchemaUpgradeIsIdempotentForFreshAndExistingDatabase() throws Exception {
        var script = new ResourceDatabasePopulator(new ClassPathResource("db/profile-schema.sql"));
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:profile-schema-upgrade", "sa", "")) {
            script.populate(connection);
            try (var statement = connection.createStatement()) {
                statement.execute("create table groups (group_id bigint primary key, university_id bigint not null)");
            }
            script.populate(connection);
            script.populate(connection);
            try (var columns = connection.getMetaData().getColumns(null, null, "GROUPS", "UNIVERSITY_ID")) {
                assertTrue(columns.next());
                assertThat(columns.getInt("NULLABLE")).isEqualTo(java.sql.DatabaseMetaData.columnNullable);
            }
        }
    }

    @Test
    void simultaneousFirstScoresKeepMaximumAndSingleRow() throws Exception {
        User student = user(Role.STUDENT);
        var executor = Executors.newFixedThreadPool(2);
        var firstSaved = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        try {
            var first = executor.submit(() -> new TransactionTemplate(transactionManager).execute(status -> {
                try {
                    saveScore(student, 5, 3);
                    firstSaved.countDown();
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                    return true;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }));
            assertTrue(firstSaved.await(5, TimeUnit.SECONDS));
            var second = executor.submit(() -> {
                secondStarted.countDown();
                saveScore(student, 5, 10);
                return true;
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            try {
                assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS));
            } finally {
                release.countDown();
            }
            assertTrue(first.get(5, TimeUnit.SECONDS));
            assertTrue(second.get(5, TimeUnit.SECONDS));
            assertThat(scoreVector(student).get(5)).isEqualTo(10);
            assertThat(scores.findByUserIdIn(List.of(student.getId()))).hasSize(1);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private User user(Role role) {
        User user = new User();
        user.setFirstName("Имя");
        user.setLastName("Фамилия");
        user.setMiddleName("Отчество");
        user.setEmail(UUID.randomUUID() + "@example.test");
        user.setPassword("unused-for-jwt-tests");
        user.setRole(role);
        user.setEnabled(true);
        return users.save(user);
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, User user) {
        return request.header("Authorization", "Bearer " + jwtCore.generateAccessToken(UserDetailsImpl.build(user)));
    }

    private JsonNode read(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode createGroup(User teacher, String name, String organization) throws Exception {
        return read(mvc.perform(auth(post("/groups"), teacher).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("name", name, "organization", organization))))
                .andExpect(status().isCreated()).andReturn());
    }

    private JsonNode groupDetail(User teacher, long id) throws Exception {
        return read(mvc.perform(auth(get("/groups/" + id), teacher)).andExpect(status().isOk()).andReturn());
    }

    private void saveScore(User user, int level, int score) throws Exception {
        mvc.perform(auth(post("/profile/scores"), user).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("level", level, "score", score))))
                .andExpect(status().isOk());
    }

    private List<Integer> scoreVector(User user) throws Exception {
        return Arrays.asList(mapper.readValue(mvc.perform(auth(get("/profile/scores"), user))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Integer[].class));
    }

    private MockMultipartFile pdf() {
        return new MockMultipartFile("file", "employment.pdf", "application/pdf", "%PDF-1.4\nTest employment document".getBytes());
    }

    private MockHttpServletRequestBuilder teacherRequest(User user, MockMultipartFile file) {
        return auth(multipart("/profile/teacher-request").file(file).param("fullName", "Фамилия Имя Отчество")
                .param("organization", "Юридическое название вуза"), user);
    }

    private String text(Part part) throws Exception {
        Object contents = part.getContent();
        if (contents instanceof String text) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        if (contents instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                result.append(text(multipart.getBodyPart(i)));
            }
        }
        return result.toString();
    }

    private List<String> attachmentNames(Part part) throws Exception {
        List<String> result = new ArrayList<>();
        if (part.getFileName() != null) {
            result.add(part.getFileName());
        }
        if (part.getContent() instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                result.addAll(attachmentNames(multipart.getBodyPart(i)));
            }
        }
        return result;
    }
}
