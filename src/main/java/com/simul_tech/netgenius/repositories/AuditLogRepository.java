package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
