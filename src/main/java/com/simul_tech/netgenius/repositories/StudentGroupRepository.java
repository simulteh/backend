package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    List<StudentGroup> findByStudentId(Long studentId);
    List<StudentGroup> findByGroupId(Long groupId);
    Optional<StudentGroup> findByStudentIdAndGroupId(Long studentId, Long groupId);
    boolean existsByStudentIdAndGroupId(Long studentId, Long groupId);
    long countByGroupId(Long groupId);
    void deleteByStudentIdAndGroupId(Long studentId, Long groupId);
}
