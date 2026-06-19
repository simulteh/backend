package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByTeacherId(Long teacherId);
    Optional<Group> findByIdAndTeacherId(Long id, Long teacherId);
}
