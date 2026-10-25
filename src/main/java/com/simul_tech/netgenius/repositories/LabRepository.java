package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabRepository extends JpaRepository<Lab, Long> {
    List<Lab> findByActiveTrueOrderBySortOrderAsc();
}
