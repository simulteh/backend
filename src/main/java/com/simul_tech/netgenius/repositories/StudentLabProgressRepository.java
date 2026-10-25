package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.StudentLabProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentLabProgressRepository extends JpaRepository<StudentLabProgress, Long> {
    Optional<StudentLabProgress> findByStudentIdAndLabId(Long studentId, Long labId);
    /** Лидерборд по лабораторной: лучшие оценки студентов по убыванию. */
    List<StudentLabProgress> findByLabIdAndBestScoreNotNullOrderByBestScoreDesc(Long labId);
    List<StudentLabProgress> findByStudentIdIn(List<Long> studentIds);
}
