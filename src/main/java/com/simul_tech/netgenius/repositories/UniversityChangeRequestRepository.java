package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.UniversityChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UniversityChangeRequestRepository extends JpaRepository<UniversityChangeRequest, Long> {
    List<UniversityChangeRequest> findByStudentId(Long studentId);
    List<UniversityChangeRequest> findByStatus(VerificationStatus status);
}
