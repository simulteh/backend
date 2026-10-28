package com.simul_tech.netgenius;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simul_tech.netgenius.enums.*;
import com.simul_tech.netgenius.impls.TokenType;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.*;
import com.simul_tech.netgenius.repositories.*;
import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.security.MailTokenCipher;
import com.simul_tech.netgenius.services.*;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Regression scenarios found in the backend audit. SMTP is simulated; business services and DB are real. */
@SpringBootTest(properties = {
        "app.frontend-url=https://frontend.example.test", "app.teacher-request-email=review@example.test",
        "app.mail.from=support@example.test"
})
@AutoConfigureMockMvc
class BackendAuditFixTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired TeacherRepository teachers;
    @Autowired StudentRepository students;
    @Autowired VerificationRequestRepository verifications;
    @Autowired GroupRepository groups;
    @Autowired GroupInvitationRepository invitations;
    @Autowired LicenseRepository licenses;
    @Autowired LabRepository labs;
    @Autowired StudentLabProgressRepository progress;
    @Autowired LabAttemptRepository attempts;
    @Autowired MailOutboxRepository outbox;
    @Autowired TokenRepository tokenRepository;
    @Autowired JwtCore jwt;
    @Autowired TokenService tokens;
    @Autowired UserService userService;
    @Autowired MailOutboxService mailOutbox;
    @Autowired MailOutboxDispatcher dispatcher;
    @Autowired MailTokenCipher cipher;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean JavaMailSender mail;
    @MockitoSpyBean AuditService audit;
    static final String PASSWORD = "audit-local-123";

    @BeforeEach void mailStub() {
        when(mail.createMimeMessage()).thenAnswer(x -> new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test void oldTeacherJsonDoesNotCreatePendingOrBlockDocumentSubmission() throws Exception {
        User candidate = user(Role.USER);
        mvc.perform(json(post("/verification/teacher"), candidate, Map.of("fullName", "Audit Teacher")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("/profile/teacher-request")));
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).isEmpty();
        assertThat(teachers.findByUserId(candidate.getId())).isEmpty();
        mvc.perform(teacherRequest(candidate)).andExpect(status().isOk());
        verify(mail).send(any(MimeMessage.class));
    }

    @Test void emailChangeRevokesOldResetAndVerificationLinksAndRefreshSessions() throws Exception {
        User candidate = user(Role.USER);
        String reset = tokens.createResetToken(candidate.getId());
        String verify = tokens.createVerifyToken(candidate.getId());
        String refresh = read(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", candidate.getEmail(), "password", PASSWORD))))
                .andReturn()).path("refreshToken").asText();
        String newEmail = UUID.randomUUID() + "@example.test";
        mvc.perform(json(put("/users/email"), candidate, Map.of("newEmail", newEmail, "password", PASSWORD)))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("token", reset, "newPassword", "old-link-password"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/confirm-email").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("token", verify))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isBadRequest());
        assertThat(users.findById(candidate.getId()).orElseThrow().isEnabled()).isFalse();
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", newEmail, "password", PASSWORD))))
                .andExpect(status().isOk());
    }

    @Test void failedResetEmailIsRetriedWithoutInvalidatingPreviouslyDeliveredLink() throws Exception {
        User candidate = user(Role.USER);
        String previous = tokens.createResetToken(candidate.getId());
        failMail();
        forgot(candidate);
        MailOutbox entry = entry(candidate, TokenType.RESET);
        assertThat(entry.getStatus()).isEqualTo(MailOutbox.Status.PENDING);
        assertThat(entry.getAttempts()).isEqualTo(1);
        assertThat(entry.getNextAttemptAt()).isAfter(Instant.now());
        String next = cipher.decrypt(entry.getEncryptedToken(), entry.encryptionContext());
        assertThat(entry.getEncryptedToken()).doesNotContain(next);
        assertThat(tokenRepository.findActiveTokensByUserId(candidate.getId()))
                .anyMatch(t -> t.getDetails().getTokenHash().equals(Token.hash(previous)));
        doNothing().when(mail).send(any(MimeMessage.class));
        due(entry);
        dispatcher.retryPending();
        MailOutbox sent = outbox.findById(entry.getId()).orElseThrow();
        assertThat(sent.getStatus()).isEqualTo(MailOutbox.Status.SENT);
        assertThat(sent.getAttempts()).isEqualTo(2);
        assertThat(sent.getEncryptedToken()).isNull();
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("token", previous, "newPassword", "previous-link-password"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("token", next, "newPassword", "retried-link-password"))))
                .andExpect(status().isOk());
        verify(mail, times(2)).send(any(MimeMessage.class));
    }

    @Test void registrationSurvivesSmtpFailureAndConfirmationIsRetried() throws Exception {
        failMail();
        String email = UUID.randomUUID() + "@example.test";
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("firstName", "Audit", "lastName", "User", "email", email, "password", PASSWORD))))
                .andExpect(status().isCreated());
        User candidate = users.findByEmail(email).orElseThrow();
        assertThat(candidate.isEnabled()).isFalse();
        MailOutbox entry = entry(candidate, TokenType.VERIFY);
        String raw = cipher.decrypt(entry.getEncryptedToken(), entry.encryptionContext());
        doNothing().when(mail).send(any(MimeMessage.class));
        due(entry);
        dispatcher.retryPending();
        mvc.perform(post("/auth/confirm-email").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("token", raw)))).andExpect(status().isOk());
        assertThat(users.findById(candidate.getId()).orElseThrow().isEnabled()).isTrue();
    }

    @Test void rolledBackTransactionDoesNotSendOrRetainQueuedEmail() {
        User candidate = user(Role.USER);
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager).execute(status -> {
            userService.sendResetToken(candidate.getEmail());
            throw new IllegalStateException("simulate business rollback");
        }));
        assertThat(outbox.findAll().stream().filter(m -> m.getUserId().equals(candidate.getId()))).isEmpty();
        assertThat(tokenRepository.findActiveTokensByUserId(candidate.getId())).isEmpty();
        verify(mail, never()).send(any(MimeMessage.class));
    }

    @Test void queuedResetIsCancelledAfterEmailChangeAndCannotBeSentToPreviousAddress() throws Exception {
        User candidate = user(Role.USER);
        failMail(); forgot(candidate);
        MailOutbox entry = entry(candidate, TokenType.RESET);
        mvc.perform(json(put("/users/email"), candidate,
                Map.of("newEmail", UUID.randomUUID() + "@example.test", "password", PASSWORD)))
                .andExpect(status().isOk());
        due(entry);
        mailOutbox.deliver(entry.getId());
        assertThat(outbox.findById(entry.getId()).orElseThrow().getStatus()).isEqualTo(MailOutbox.Status.CANCELLED);
        assertThat(outbox.findById(entry.getId()).orElseThrow().getEncryptedToken()).isNull();
        verify(mail, times(2)).send(any(MimeMessage.class));
    }

    @Test void queuedResetIsCancelledAfterPasswordChange() throws Exception {
        User candidate = user(Role.USER);
        failMail(); forgot(candidate);
        MailOutbox entry = entry(candidate, TokenType.RESET);
        mvc.perform(json(post("/auth/change-password"), candidate,
                Map.of("oldPassword", PASSWORD, "newPassword", "changed-password"))).andExpect(status().isOk());
        due(entry); mailOutbox.deliver(entry.getId());
        assertThat(outbox.findById(entry.getId()).orElseThrow().getStatus()).isEqualTo(MailOutbox.Status.CANCELLED);
        verify(mail).send(any(MimeMessage.class));
    }

    @Test void pendingMailIsSupersededAndExpiredMailIsNotSent() throws Exception {
        User candidate = user(Role.USER);
        failMail(); forgot(candidate);
        MailOutbox previous = entry(candidate, TokenType.RESET);
        forgot(candidate);
        assertThat(outbox.findById(previous.getId()).orElseThrow().getStatus()).isEqualTo(MailOutbox.Status.CANCELLED);
        assertThat(outbox.findById(previous.getId()).orElseThrow().getEncryptedToken()).isNull();
        MailOutbox newest = entry(candidate, TokenType.RESET);
        newest.setExpiresAt(Instant.now().minusSeconds(1)); due(newest);
        mailOutbox.deliver(newest.getId());
        assertThat(outbox.findById(newest.getId()).orElseThrow().getStatus()).isEqualTo(MailOutbox.Status.CANCELLED);
        verify(mail, times(2)).send(any(MimeMessage.class));
    }

    @Test void usingPreviousDeliveredLinkCancelsPendingReplacement() throws Exception {
        User candidate = user(Role.USER);
        String previous = tokens.createResetToken(candidate.getId());
        failMail(); forgot(candidate);
        MailOutbox pending = entry(candidate, TokenType.RESET);
        mvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("token", previous, "newPassword", "previous-link-still-valid")))).andExpect(status().isOk());
        due(pending); mailOutbox.deliver(pending.getId());
        assertThat(outbox.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(MailOutbox.Status.CANCELLED);
        verify(mail).send(any(MimeMessage.class));
    }

    @Test void queuedTokenCannotBeDecryptedWithChangedContextOrCiphertext() throws Exception {
        User candidate = user(Role.USER);
        failMail(); forgot(candidate);
        MailOutbox entry = entry(candidate, TokenType.RESET);
        assertThrows(IllegalStateException.class, () -> cipher.decrypt(entry.getEncryptedToken(), entry.encryptionContext() + "changed"));
        byte[] changed = Base64.getDecoder().decode(entry.getEncryptedToken()); changed[changed.length - 1] ^= 1;
        assertThrows(IllegalStateException.class, () -> cipher.decrypt(Base64.getEncoder().encodeToString(changed), entry.encryptionContext()));
    }

    @Test void concurrentMailWorkersSendPendingEntryOnce() throws Exception {
        User candidate = user(Role.USER);
        failMail(); forgot(candidate); MailOutbox pending = entry(candidate, TokenType.RESET); due(pending);
        CountDownLatch ready = new CountDownLatch(1), release = new CountDownLatch(1), secondStarted = new CountDownLatch(1);
        doAnswer(call -> { ready.countDown(); assertTrue(release.await(5, TimeUnit.SECONDS)); return null; })
                .when(mail).send(any(MimeMessage.class));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = pool.submit(() -> mailOutbox.deliver(pending.getId()));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            Future<?> second = pool.submit(() -> { secondStarted.countDown(); mailOutbox.deliver(pending.getId()); });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            try { assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS)); }
            finally { release.countDown(); }
            first.get(5, TimeUnit.SECONDS); second.get(5, TimeUnit.SECONDS);
            assertThat(outbox.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(MailOutbox.Status.SENT);
            assertThat(outbox.findById(pending.getId()).orElseThrow().getAttempts()).isEqualTo(2);
            verify(mail, times(2)).send(any(MimeMessage.class));
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }

    @Test void longRejectionCommentDoesNotReviewApplication() throws Exception {
        User candidate = user(Role.USER); long id = submitStudent(candidate);
        mvc.perform(json(post("/admin/verifications/" + id + "/reject"), admin(), Map.of("comment", "x".repeat(1001))))
                .andExpect(status().isBadRequest());
        assertThat(verifications.findById(id).orElseThrow().getStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @ParameterizedTest @ValueSource(strings = {"student", "teacher"})
    void rejectedApplicationAllowsChoosingOtherType(String firstType) throws Exception {
        User candidate = user(Role.USER);
        if (firstType.equals("student")) submitStudent(candidate);
        else mvc.perform(teacherRequest(candidate)).andExpect(status().isOk());
        long id = verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId()).get(0).getId();
        mvc.perform(json(post("/admin/verifications/" + id + "/reject"), admin(), Map.of("comment", "Wrong role")))
                .andExpect(status().isOk());
        if (firstType.equals("student")) mvc.perform(teacherRequest(candidate)).andExpect(status().isOk());
        else submitStudent(candidate);
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId()))
                .filteredOn(r -> r.getStatus() == VerificationStatus.PENDING).hasSize(1);
        assertThat(users.findById(candidate.getId()).orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    @ParameterizedTest @ValueSource(strings = {"fullName", "faculty", "phone", "speciality", "direction", "degree"})
    void oversizedStudentFieldsReturnBadRequestWithoutSendingOrPersisting(String field) throws Exception {
        User candidate = user(Role.USER);
        Map<String, Object> body = new HashMap<>(Map.of("fullName", "Audit Student"));
        body.put(field, "x".repeat(256));
        mvc.perform(json(post("/verification/student"), candidate, body)).andExpect(status().isBadRequest());
        assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).isEmpty();
        verify(mail, never()).send(any(MimeMessage.class));
    }

    @ParameterizedTest @ValueSource(strings = {"{\"birthYear\":2101}", "{\"birthYear\":1899}", "{\"course\":0}", "{\"universityId\":-1}"})
    void invalidStudentNumbersAreRejected(String fields) throws Exception {
        User candidate = user(Role.USER);
        Map<String, Object> body = mapper.readValue(fields, Map.class);
        body.put("fullName", "Audit Student");
        mvc.perform(json(post("/verification/student"), candidate, body)).andExpect(status().isBadRequest());
    }

    @ParameterizedTest @ValueSource(strings = {"studentId", "email"})
    void expiredInvitationDoesNotBlockRenewalWithoutStudentVisitingList(String target) throws Exception {
        User owner = user(Role.TEACHER);
        Teacher teacher = teacher(owner);
        User invited = user(Role.STUDENT);
        Student student = student(invited);
        Group group = group(teacher);
        GroupInvitation expired = new GroupInvitation();
        expired.setGroupId(group.getId()); expired.setTeacherId(teacher.getId());
        if (target.equals("studentId")) expired.setStudentId(student.getId());
        else expired.setEmail(invited.getEmail());
        expired.setInviteToken(UUID.randomUUID().toString());
        expired.setExpiresAt(Instant.now().minusSeconds(60)); invitations.save(expired);
        Object value = target.equals("studentId") ? student.getId() : invited.getEmail();
        mvc.perform(json(post("/teacher/groups/" + group.getId() + "/invite"), owner, Map.of(target, value)))
                .andExpect(status().isCreated());
        mvc.perform(json(post("/teacher/groups/" + group.getId() + "/invite"), owner, Map.of(target, value)))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void repeatedSyncCountsOneAttemptWithOrWithoutEventId(boolean hasId) throws Exception {
        User candidate = licensedStudent(); Lab lab = lab();
        Map<String, Object> event = event(lab);
        if (hasId) event.put("eventId", UUID.randomUUID().toString());
        Object body = syncBody(event);
        mvc.perform(json(post("/sync/progress"), candidate, body)).andExpect(status().isOk()).andExpect(jsonPath("$.synced").value(1));
        mvc.perform(json(post("/sync/progress"), candidate, body)).andExpect(status().isOk()).andExpect(jsonPath("$.synced").value(0));
        long studentId = students.findByUserId(candidate.getId()).orElseThrow().getId();
        assertThat(progress.findByStudentIdAndLabId(studentId, lab.getId()).orElseThrow().getAttemptsCount()).isEqualTo(1);
        assertThat(attempts.findAll().stream().filter(a -> a.getStudentId().equals(studentId))).hasSize(1);
    }

    @Test void sameEventIdWithDifferentPayloadIsConflictAndOtherAttemptsRemainDistinct() throws Exception {
        User candidate = licensedStudent(); Lab lab = lab(); Map<String, Object> event = event(lab);
        event.put("eventId", UUID.randomUUID().toString());
        mvc.perform(json(post("/sync/progress"), candidate, syncBody(event))).andExpect(status().isOk());
        event.put("score", 90);
        mvc.perform(json(post("/sync/progress"), candidate, syncBody(event))).andExpect(status().isConflict());
        event.put("eventId", UUID.randomUUID().toString());
        mvc.perform(json(post("/sync/progress"), candidate, syncBody(event))).andExpect(status().isOk());
        long studentId = students.findByUserId(candidate.getId()).orElseThrow().getId();
        StudentLabProgress saved = progress.findByStudentIdAndLabId(studentId, lab.getId()).orElseThrow();
        assertThat(saved.getAttemptsCount()).isEqualTo(2); assertThat(saved.getBestScore()).isEqualTo(90);
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void concurrentSyncSerializesFirstInsertAndDuplicateDetection(boolean duplicate) throws Exception {
        User candidate = licensedStudent(); Lab lab = lab();
        long studentId = students.findByUserId(candidate.getId()).orElseThrow().getId();
        Map<String, Object> event = event(lab); event.put("eventId", UUID.randomUUID().toString());
        CountDownLatch ready = new CountDownLatch(1), release = new CountDownLatch(1), secondStarted = new CountDownLatch(1);
        doAnswer(call -> {
            call.callRealMethod(); ready.countDown(); assertTrue(release.await(5, TimeUnit.SECONDS)); return null;
        }).when(audit).log(eq(candidate.getId()), eq("SYNC"), eq(studentId), eq("SYNC"), isNull(), anyString());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Map<String, Object> other = new HashMap<>(event);
        if (!duplicate) { other.put("eventId", UUID.randomUUID().toString()); other.put("score", 90); }
        try {
            Future<MvcResult> first = pool.submit(() -> mvc.perform(json(post("/sync/progress"), candidate, syncBody(event))).andReturn());
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            Future<MvcResult> second = pool.submit(() -> {
                secondStarted.countDown(); return mvc.perform(json(post("/sync/progress"), candidate, syncBody(other))).andReturn();
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            try { assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS)); }
            finally { release.countDown(); }
            assertThat(first.get(5, TimeUnit.SECONDS).getResponse().getStatus()).isEqualTo(200);
            MvcResult secondResult = second.get(5, TimeUnit.SECONDS);
            assertThat(secondResult.getResponse().getStatus()).isEqualTo(200);
            assertThat(read(secondResult).path("synced").asInt()).isEqualTo(duplicate ? 0 : 1);
            StudentLabProgress saved = progress.findByStudentIdAndLabId(studentId, lab.getId()).orElseThrow();
            assertThat(saved.getAttemptsCount()).isEqualTo(duplicate ? 1 : 2);
            assertThat(saved.getBestScore()).isEqualTo(duplicate ? 85 : 90);
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }

    @ParameterizedTest @ValueSource(strings = {"negative", "tooHigh", "future", "reverse", "noTime", "noStatus"})
    void invalidSyncIsRejectedWithoutProgress(String kind) throws Exception {
        User candidate = licensedStudent(); Lab lab = lab(); Map<String, Object> event = event(lab);
        switch (kind) {
            case "negative" -> event.put("score", -100);
            case "tooHigh" -> event.put("score", 101);
            case "future" -> event.put("completedAt", Instant.now().plus(365, ChronoUnit.DAYS).toString());
            case "reverse" -> event.put("completedAt", Instant.now().minusSeconds(3600).toString());
            case "noTime" -> { event.remove("startedAt"); event.remove("completedAt"); }
            case "noStatus" -> event.remove("status");
        }
        mvc.perform(json(post("/sync/progress"), candidate, syncBody(event))).andExpect(status().isBadRequest());
        long studentId = students.findByUserId(candidate.getId()).orElseThrow().getId();
        assertThat(progress.findByStudentIdAndLabId(studentId, lab.getId())).isEmpty();
    }

    @ParameterizedTest @ValueSource(strings = {"approve", "reject"})
    void concurrentReviewReturnsOneSuccessAndOneConflict(String firstAction) throws Exception {
        User candidate = user(Role.USER); long id = submitStudent(candidate); User admin = admin();
        String secondAction = firstAction.equals("approve") ? "reject" : "approve";
        CountDownLatch firstReady = new CountDownLatch(1), release = new CountDownLatch(1), secondStarted = new CountDownLatch(1);
        doAnswer(call -> {
            call.callRealMethod(); firstReady.countDown(); assertTrue(release.await(5, TimeUnit.SECONDS)); return null;
        }).when(audit).log(anyLong(), eq("VERIFICATION_REQUEST"), eq(id), eq(firstAction.toUpperCase(Locale.ROOT)));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = pool.submit(() -> review(id, firstAction, admin));
            assertTrue(firstReady.await(5, TimeUnit.SECONDS));
            Future<Integer> second = pool.submit(() -> { secondStarted.countDown(); return review(id, secondAction, admin); });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            try { assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS)); }
            finally { release.countDown(); }
            assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo(200);
            assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo(409);
            boolean approved = firstAction.equals("approve");
            assertThat(verifications.findById(id).orElseThrow().getStatus())
                    .isEqualTo(approved ? VerificationStatus.APPROVED : VerificationStatus.REJECTED);
            assertThat(users.findById(candidate.getId()).orElseThrow().getRole()).isEqualTo(approved ? Role.STUDENT : Role.USER);
            assertThat(students.findByUserId(candidate.getId()).orElseThrow().isVerified()).isEqualTo(approved);
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }

    @Test void concurrentStudentAndDocumentTeacherSubmissionCreateOnePending() throws Exception {
        User candidate = user(Role.USER);
        CountDownLatch firstReady = new CountDownLatch(1), release = new CountDownLatch(1), secondStarted = new CountDownLatch(1);
        doAnswer(call -> { firstReady.countDown(); assertTrue(release.await(5, TimeUnit.SECONDS)); return null; })
                .when(mail).send(any(MimeMessage.class));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Long> first = pool.submit(() -> submitStudent(candidate));
            assertTrue(firstReady.await(5, TimeUnit.SECONDS));
            Future<Integer> second = pool.submit(() -> {
                secondStarted.countDown(); return mvc.perform(teacherRequest(candidate)).andReturn().getResponse().getStatus();
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            try { assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS)); }
            finally { release.countDown(); }
            assertThat(first.get(5, TimeUnit.SECONDS)).isPositive();
            assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo(409);
            assertThat(verifications.findByUserIdOrderByCreatedAtDesc(candidate.getId())).hasSize(1);
            assertThat(teachers.existsByUserId(candidate.getId())).isFalse();
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }

    private void failMail() { doThrow(new MailSendException("simulated SMTP outage")).when(mail).send(any(MimeMessage.class)); }
    private void forgot(User user) throws Exception {
        mvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", user.getEmail())))).andExpect(status().isOk());
    }
    private MailOutbox entry(User user, TokenType type) {
        return outbox.findAll().stream().filter(e -> e.getUserId().equals(user.getId()) && e.getType() == type)
                .max(Comparator.comparing(MailOutbox::getId)).orElseThrow();
    }
    private void due(MailOutbox entry) { entry.setNextAttemptAt(Instant.now().minusSeconds(1)); outbox.save(entry); }
    private int review(long id, String action, User admin) throws Exception {
        return mvc.perform(json(post("/admin/verifications/" + id + "/" + action), admin, Map.of("comment", "Audit review")))
                .andReturn().getResponse().getStatus();
    }
    private long submitStudent(User user) throws Exception {
        return read(mvc.perform(json(post("/verification/student"), user, Map.of("fullName", "Audit Student")))
                .andExpect(status().isCreated()).andReturn()).path("id").asLong();
    }
    private User user(Role role) {
        User user = new User(); user.setFirstName("Audit"); user.setLastName("User");
        user.setEmail(UUID.randomUUID() + "@example.test"); user.setPassword(encoder.encode(PASSWORD));
        user.setRole(role); user.setEnabled(true); return users.save(user);
    }
    private User admin() { return users.findByEmail("admin@netgenius.ru").orElseThrow(); }
    private Teacher teacher(User user) {
        Teacher t = new Teacher(); t.setUserId(user.getId()); t.setFullName("Audit Teacher"); t.setVerified(true); return teachers.save(t);
    }
    private Student student(User user) {
        Student s = new Student(); s.setUserId(user.getId()); s.setFullName("Audit Student"); s.setVerified(true); return students.save(s);
    }
    private Group group(Teacher teacher) {
        Group g = new Group(); g.setTeacherId(teacher.getId()); g.setGroupName("Audit Group"); return groups.save(g);
    }
    private User licensedStudent() {
        User user = user(Role.STUDENT); student(user); License l = new License();
        l.setLicenseKey(UUID.randomUUID().toString()); l.setOwnerUserId(user.getId());
        l.setActivatedAt(Instant.now()); l.setExpiresAt(Instant.now().plus(365, ChronoUnit.DAYS)); l.setActive(true);
        licenses.save(l); return user;
    }
    private Lab lab() { Lab lab = new Lab(); lab.setTitle("Audit Lab"); return labs.save(lab); }
    private Map<String, Object> event(Lab lab) {
        return new HashMap<>(Map.of("labId", lab.getId(), "status", "COMPLETED", "score", 85,
                "startedAt", Instant.now().minusSeconds(60).toString(), "completedAt", Instant.now().minusSeconds(1).toString()));
    }
    private Object syncBody(Map<String, Object> event) { return Map.of("deviceId", "audit-device", "events", List.of(event)); }
    private MockHttpServletRequestBuilder teacherRequest(User user) {
        return auth(multipart("/profile/teacher-request").file(new MockMultipartFile(
                "file", "proof.pdf", "application/pdf", "%PDF-1.4 test".getBytes()))
                .param("fullName", "Audit Teacher").param("organization", "Audit University"), user);
    }
    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, User user) {
        return request.header("Authorization", "Bearer " + jwt.generateAccessToken(UserDetailsImpl.build(user)));
    }
    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, User user, Object body) throws Exception {
        return auth(request, user).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body));
    }
    private JsonNode read(MvcResult result) throws Exception { return mapper.readTree(result.getResponse().getContentAsByteArray()); }
}
