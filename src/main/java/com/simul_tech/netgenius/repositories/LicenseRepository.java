package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.License;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {
    /** Блокирующая выборка для атомарной активации (защита от двойной активации одного ключа). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<License> findWithLockByLicenseKey(String licenseKey);
    boolean existsByLicenseKey(String licenseKey);
    List<License> findByOwnerUserId(Long ownerUserId);
}
