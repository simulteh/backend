package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.LabAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LabAttemptRepository extends JpaRepository<LabAttempt, Long> {
    Optional<LabAttempt> findByStudentIdAndEventKey(Long studentId, String eventKey);
}
