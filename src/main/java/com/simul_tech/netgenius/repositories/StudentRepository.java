package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByFullNameContainingIgnoreCase(String partialName);
    List<Student> findByGroup(String groupName);
    List<Student> findByGroupContaining(String groupPart);
    List<Student> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);
    List<Student> findByFullNameAndGroup(String fullName, String group);
}