package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
