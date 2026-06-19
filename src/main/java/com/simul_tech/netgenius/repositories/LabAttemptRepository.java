package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.LabAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabAttemptRepository extends JpaRepository<LabAttempt, Long> {
    List<LabAttempt> findByStudentId(Long studentId);
    List<LabAttempt> findByLabId(Long labId);
    List<LabAttempt> findByStudentIdAndLabId(Long studentId, Long labId);
}
