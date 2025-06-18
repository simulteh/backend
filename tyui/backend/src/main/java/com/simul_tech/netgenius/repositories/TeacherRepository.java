package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    Optional<Teacher> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<Teacher> findByIsActive(Boolean isActive);
    
    List<Teacher> findByDepartment(String department);
    
    List<Teacher> findBySpecialization(String specialization);
    
    List<Teacher> findByAcademicDegree(String academicDegree);
    
    List<Teacher> findByAcademicTitle(String academicTitle);
    
    List<Teacher> findByExperienceYearsBetween(Integer minYears, Integer maxYears);
    
    List<Teacher> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    @Query("SELECT t FROM Teacher t WHERE " +
            "(:firstName IS NULL OR LOWER(t.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(t.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:email IS NULL OR LOWER(t.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:specialization IS NULL OR LOWER(t.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
            "(:department IS NULL OR LOWER(t.department) LIKE LOWER(CONCAT('%', :department, '%'))) AND " +
            "(:academicDegree IS NULL OR t.academicDegree = :academicDegree) AND " +
            "(:academicTitle IS NULL OR t.academicTitle = :academicTitle) AND " +
            "(:minExperienceYears IS NULL OR t.experienceYears >= :minExperienceYears) AND " +
            "(:maxExperienceYears IS NULL OR t.experienceYears <= :maxExperienceYears) AND " +
            "(:isActive IS NULL OR t.isActive = :isActive)")
    Page<Teacher> findTeachersByFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("specialization") String specialization,
            @Param("department") String department,
            @Param("academicDegree") String academicDegree,
            @Param("academicTitle") String academicTitle,
            @Param("minExperienceYears") Integer minExperienceYears,
            @Param("maxExperienceYears") Integer maxExperienceYears,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
} 