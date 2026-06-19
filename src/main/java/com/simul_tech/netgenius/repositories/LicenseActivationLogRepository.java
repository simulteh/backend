package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.LicenseActivationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LicenseActivationLogRepository extends JpaRepository<LicenseActivationLog, Long> {
    List<LicenseActivationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<LicenseActivationLog> findByLicenseIdOrderByCreatedAtDesc(Long licenseId);
}
