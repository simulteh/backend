package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByScore(Integer score);
}