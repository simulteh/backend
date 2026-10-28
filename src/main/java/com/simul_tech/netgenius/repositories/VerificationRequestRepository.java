package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    @Query("select r.userId from VerificationRequest r where r.id = :id")
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from VerificationRequest r where r.id = :id")
    Optional<VerificationRequest> findByIdForUpdate(@Param("id") Long id);

    List<VerificationRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<VerificationRequest> findByStatus(VerificationStatus status);
    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);
}
