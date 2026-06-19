package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.models.AuditLog;
import com.simul_tech.netgenius.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Журналирование действий (требование безопасности: Audit Logging Required).
 * Запись ведётся в отдельной транзакции, чтобы сбой аудита не откатывал бизнес-операцию.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorUserId, String entityType, Long entityId, String action,
                    String oldValue, String newValue) {
        try {
            AuditLog entry = new AuditLog();
            entry.setActorUserId(actorUserId);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setAction(action);
            entry.setOldValue(truncate(oldValue));
            entry.setNewValue(truncate(newValue));
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Не удалось записать аудит [{} {} {}]: {}", entityType, entityId, action, e.getMessage());
        }
    }

    public void log(Long actorUserId, String entityType, Long entityId, String action) {
        log(actorUserId, entityType, entityId, action, null, null);
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }
}
