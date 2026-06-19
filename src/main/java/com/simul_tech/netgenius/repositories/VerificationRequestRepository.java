package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    List<VerificationRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<VerificationRequest> findByStatus(VerificationStatus status);
    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);
}
