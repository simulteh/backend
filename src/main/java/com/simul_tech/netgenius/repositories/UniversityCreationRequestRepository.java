package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.models.UniversityCreationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UniversityCreationRequestRepository extends JpaRepository<UniversityCreationRequest, Long> {
    List<UniversityCreationRequest> findByStatus(VerificationStatus status);
    List<UniversityCreationRequest> findByTeacherId(Long teacherId);
}
