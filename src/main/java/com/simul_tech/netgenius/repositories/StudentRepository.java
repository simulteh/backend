package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
