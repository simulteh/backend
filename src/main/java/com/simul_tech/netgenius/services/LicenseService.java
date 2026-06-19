package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.license.*;
import com.simul_tech.netgenius.enums.LicenseAction;
import com.simul_tech.netgenius.exceptions.ConflictException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.License;
import com.simul_tech.netgenius.models.LicenseActivationLog;
import com.simul_tech.netgenius.models.LicenseBatch;
import com.simul_tech.netgenius.repositories.LicenseActivationLogRepository;
import com.simul_tech.netgenius.repositories.LicenseBatchRepository;
import com.simul_tech.netgenius.repositories.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Управление лицензиями симулятора: активация пользователем, история, выпуск
 * администратором (одиночный и партиями). Лицензия действует 365 дней с момента
 * активации, ключ активируется только один раз.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LicenseService {

    /** Срок действия лицензии после активации. */
    private static final Duration LICENSE_DURATION = Duration.ofDays(365);
    private static final String ENTITY_TYPE = "LICENSE";

    private final LicenseRepository licenseRepository;
    private final LicenseBatchRepository licenseBatchRepository;
    private final LicenseActivationLogRepository licenseActivationLogRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    /** POST /licenses/activate. */
    public ActivatedLicenseResponse activate(String licenseKey) {
        currentUserService.requireEmailConfirmed();
        Long currentUserId = currentUserService.getCurrentUserId();

        // Блокирующая выборка: гарантирует, что параллельные запросы не активируют один ключ дважды.
        License license = licenseRepository.findWithLockByLicenseKey(licenseKey)
                .orElseThrow(() -> new ResourceNotFoundException("Лицензия не найдена"));

        if (license.isActivated() || license.getOwnerUserId() != null) {
            throw new ConflictException("ключ уже активирован");
        }

        Instant now = Instant.now();
        license.setOwnerUserId(currentUserId);
        license.setActivatedAt(now);
        license.setExpiresAt(now.plus(LICENSE_DURATION));
        license.setActive(true);
        License saved = licenseRepository.save(license);

        writeLog(saved.getId(), currentUserId, LicenseAction.ACTIVATED);
        auditService.log(currentUserId, ENTITY_TYPE, saved.getId(), LicenseAction.ACTIVATED.name());

        return ActivatedLicenseResponse.from(saved);
    }

    /** GET /licenses/me. */
    @Transactional(readOnly = true)
    public List<MyLicenseResponse> myLicenses() {
        Long currentUserId = currentUserService.getCurrentUserId();
        Instant now = Instant.now();
        return licenseRepository.findByOwnerUserId(currentUserId).stream()
                .map(license -> MyLicenseResponse.from(license, now))
                .toList();
    }

    /** GET /licenses/history. */
    @Transactional(readOnly = true)
    public List<LicenseHistoryResponse> history() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return licenseActivationLogRepository.findByUserIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(LicenseHistoryResponse::from)
                .toList();
    }

    /** POST /admin/licenses/create. */
    public CreatedLicenseResponse create(CreateLicenseRequest request) {
        Long adminId = currentUserService.getCurrentUserId();

        License license = new License();
        license.setLicenseKey(generateUniqueKey());
        license.setIssuedByAdmin(adminId);
        license.setActive(true);
        license.setOwnerUserId(null);
        License saved = licenseRepository.save(license);

        writeLog(saved.getId(), adminId, LicenseAction.CREATED);
        auditService.log(adminId, ENTITY_TYPE, saved.getId(), LicenseAction.CREATED.name());

        return CreatedLicenseResponse.from(saved);
    }

    /** POST /admin/licenses/batch. */
    public LicenseBatchResponse createBatch(CreateLicenseBatchRequest request) {
        Long adminId = currentUserService.getCurrentUserId();

        LicenseBatch batch = new LicenseBatch();
        batch.setUniversityId(request.universityId());
        batch.setQuantity(request.quantity());
        batch.setCreatedByAdmin(adminId);
        LicenseBatch savedBatch = licenseBatchRepository.save(batch);

        List<String> keys = new ArrayList<>(request.quantity());
        for (int i = 0; i < request.quantity(); i++) {
            License license = new License();
            license.setLicenseKey(generateUniqueKey());
            license.setIssuedByAdmin(adminId);
            license.setBatchId(savedBatch.getId());
            license.setActive(true);
            license.setOwnerUserId(null);
            License saved = licenseRepository.save(license);

            writeLog(saved.getId(), adminId, LicenseAction.CREATED);
            keys.add(saved.getLicenseKey());
        }

        auditService.log(adminId, "LICENSE_BATCH", savedBatch.getId(), LicenseAction.CREATED.name());

        return new LicenseBatchResponse(savedBatch.getId(), savedBatch.getQuantity(), keys);
    }

    /** GET /admin/licenses. */
    @Transactional(readOnly = true)
    public List<AdminLicenseResponse> findAll() {
        return licenseRepository.findAll().stream()
                .map(AdminLicenseResponse::from)
                .toList();
    }

    /** Генерирует уникальный ключ лицензии, повторяя при коллизии. */
    private String generateUniqueKey() {
        String key;
        do {
            key = UUID.randomUUID().toString().toUpperCase();
        } while (licenseRepository.existsByLicenseKey(key));
        return key;
    }

    private void writeLog(Long licenseId, Long userId, LicenseAction action) {
        LicenseActivationLog log = new LicenseActivationLog();
        log.setLicenseId(licenseId);
        log.setUserId(userId);
        log.setAction(action);
        licenseActivationLogRepository.save(log);
    }
}
