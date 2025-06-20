package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Базовые методы CRUD уже предоставляются JpaRepository

    // Поиск по полному имени (частичное совпадение без учета регистра)
    List<Student> findByFullNameContainingIgnoreCase(String fullNamePart);

    // Поиск по группе (точное совпадение)
    List<Student> findByGroup(String group);

    // Поиск по части названия группы
    List<Student> findByGroupContaining(String groupPart);

    // Поиск по диапазону дат зачисления
    List<Student> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);

    // Поиск студентов, зачисленных после указанной даты
    List<Student> findByEnrollmentDateAfter(LocalDate startDate);

    // Поиск студентов, зачисленных до указанной даты
    List<Student> findByEnrollmentDateBefore(LocalDate endDate);

    // Комбинированный поиск по ФИО и группе
    List<Student> findByFullNameAndGroup(String fullName, String group);

    // Комбинированный поиск по части ФИО и диапазону дат
    List<Student> findByFullNameContainingAndEnrollmentDateBetween(
            String fullNamePart,
            LocalDate startDate,
            LocalDate endDate);

    // Универсальный метод фильтрации с пагинацией
    @Query("SELECT s FROM Student s WHERE " +
            "(:#{#filter.id} IS NULL OR s.id = :#{#filter.id}) AND " +
            "(:#{#filter.fullName} IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :#{#filter.fullName}, '%')) AND " +
            "(:#{#filter.group} IS NULL OR LOWER(s.group) LIKE LOWER(CONCAT('%', :#{#filter.group}, '%')) AND " +
            "(:#{#filter.startEnrollmentDate} IS NULL OR s.enrollmentDate >= :#{#filter.startEnrollmentDate}) AND " +
            "(:#{#filter.endEnrollmentDate} IS NULL OR s.enrollmentDate <= :#{#filter.endEnrollmentDate}) AND " +
            "(:#{#filter.isActive} IS NULL OR s.isActive = :#{#filter.isActive})")
    Page<Student> findByFilters(
            @Param("filter") StudentFilterRequest filter,
            Pageable pageable
    );
}