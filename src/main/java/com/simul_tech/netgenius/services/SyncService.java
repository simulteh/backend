package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.sync.SyncEventDto;
import com.simul_tech.netgenius.dto.sync.SyncProgressItem;
import com.simul_tech.netgenius.dto.sync.SyncProgressRequest;
import com.simul_tech.netgenius.dto.sync.SyncResultResponse;
import com.simul_tech.netgenius.enums.LabStatus;
import com.simul_tech.netgenius.exceptions.ForbiddenOperationException;
import com.simul_tech.netgenius.models.Lab;
import com.simul_tech.netgenius.models.LabAttempt;
import com.simul_tech.netgenius.models.License;
import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.models.StudentLabProgress;
import com.simul_tech.netgenius.repositories.LabAttemptRepository;
import com.simul_tech.netgenius.repositories.LabRepository;
import com.simul_tech.netgenius.repositories.LicenseRepository;
import com.simul_tech.netgenius.repositories.StudentLabProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Синхронизация прогресса десктоп-симулятора (требование: License Validation Required For Desktop Login).
 * Принимает накопленные в оффлайне события, проверяет действующую лицензию,
 * отбрасывает данные старше 14 дней и обновляет попытки и агрегированный прогресс.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SyncService {

    /** Максимальный возраст оффлайн-события, которое сервер ещё принимает. */
    private static final Duration OFFLINE_LIMIT = Duration.ofDays(14);

    private final LicenseRepository licenseRepository;
    private final LabRepository labRepository;
    private final LabAttemptRepository labAttemptRepository;
    private final StudentLabProgressRepository studentLabProgressRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public SyncResultResponse syncProgress(SyncProgressRequest request) {
        currentUserService.requireEmailConfirmed();
        Student student = currentUserService.getCurrentStudent();
        Long studentId = student.getId();
        Long userId = currentUserService.getCurrentUserId();

        requireValidLicense(userId);

        Instant now = Instant.now();
        Instant offlineThreshold = now.minus(OFFLINE_LIMIT);

        int synced = 0;
        int skipped = 0;
        // Сохраняем порядок появления затронутых лаб для детерминированного ответа.
        Set<Long> touchedLabIds = new LinkedHashSet<>();

        List<SyncEventDto> events = request.events();
        for (SyncEventDto event : events) {
            if (event == null || event.labId() == null) {
                skipped++;
                continue;
            }

            // Оффлайн-лимит 14 дней: отбрасываем события, чья метка времени старше лимита.
            // Возраст определяем по startedAt, а при его отсутствии — по completedAt.
            Instant eventTime = event.startedAt() != null ? event.startedAt() : event.completedAt();
            if (eventTime != null && eventTime.isBefore(offlineThreshold)) {
                skipped++;
                continue;
            }

            Lab lab = labRepository.findById(event.labId()).orElse(null);
            if (lab == null) {
                skipped++;
                continue;
            }

            Long labId = event.labId();
            recordAttempt(studentId, labId, event, request.deviceId());
            upsertProgress(studentId, labId, event, now);

            touchedLabIds.add(labId);
            synced++;
        }

        List<SyncProgressItem> progress = touchedLabIds.stream()
                .map(labId -> studentLabProgressRepository.findByStudentIdAndLabId(studentId, labId).orElse(null))
                .filter(p -> p != null)
                .map(SyncProgressItem::from)
                .toList();

        auditService.log(userId, "SYNC", studentId, "SYNC",
                null, "device=" + request.deviceId()
                        + ", synced=" + synced + ", skipped=" + skipped);

        return new SyncResultResponse(synced, skipped, progress);
    }

    /**
     * Действующая лицензия — активная и с непросроченным сроком окончания.
     * Без неё доступ к десктоп-функциям запрещён.
     */
    private void requireValidLicense(Long userId) {
        Instant now = Instant.now();
        boolean hasValid = licenseRepository.findByOwnerUserId(userId).stream()
                .anyMatch(license -> license.isActive()
                        && license.getExpiresAt() != null
                        && license.getExpiresAt().isAfter(now));
        if (!hasValid) {
            throw new ForbiddenOperationException("нет действующей лицензии");
        }
    }

    private void recordAttempt(Long studentId, Long labId, SyncEventDto event, String deviceId) {
        LabAttempt attempt = new LabAttempt();
        attempt.setStudentId(studentId);
        attempt.setLabId(labId);
        attempt.setStartedAt(event.startedAt());
        attempt.setCompletedAt(event.completedAt());
        attempt.setScore(event.score());
        attempt.setStatus(event.status());
        attempt.setClientVersion(event.clientVersion() != null ? event.clientVersion() : deviceId);
        labAttemptRepository.save(attempt);
    }

    private void upsertProgress(Long studentId, Long labId, SyncEventDto event, Instant now) {
        StudentLabProgress progress = studentLabProgressRepository
                .findByStudentIdAndLabId(studentId, labId)
                .orElseGet(() -> {
                    StudentLabProgress created = new StudentLabProgress();
                    created.setStudentId(studentId);
                    created.setLabId(labId);
                    created.setStatus(LabStatus.NOT_STARTED);
                    created.setAttemptsCount(0);
                    return created;
                });

        progress.setAttemptsCount(progress.getAttemptsCount() + 1);

        // firstStartedAt = min(существующий, startedAt).
        if (event.startedAt() != null) {
            Instant existingFirst = progress.getFirstStartedAt();
            if (existingFirst == null || event.startedAt().isBefore(existingFirst)) {
                progress.setFirstStartedAt(event.startedAt());
            }
        }

        // bestScore = max по всем попыткам.
        if (event.score() != null
                && (progress.getBestScore() == null || event.score() > progress.getBestScore())) {
            progress.setBestScore(event.score());
        }

        if (event.status() == LabStatus.COMPLETED) {
            progress.setStatus(LabStatus.COMPLETED);
            Instant completedAt = event.completedAt() != null ? event.completedAt() : now;
            Instant existingLast = progress.getLastCompletedAt();
            if (existingLast == null || completedAt.isAfter(existingLast)) {
                progress.setLastCompletedAt(completedAt);
            }
        } else if (progress.getStatus() == LabStatus.NOT_STARTED) {
            progress.setStatus(LabStatus.STARTED);
        }

        studentLabProgressRepository.save(progress);
    }
}
