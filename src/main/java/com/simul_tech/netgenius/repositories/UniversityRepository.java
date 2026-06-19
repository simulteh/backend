package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UniversityRepository extends JpaRepository<University, Long> {
    List<University> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
    boolean existsByNameIgnoreCase(String name);
}
