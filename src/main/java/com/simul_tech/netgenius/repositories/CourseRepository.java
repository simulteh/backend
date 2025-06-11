package com.simul_tech.netgenius.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Фильтрация по цене
    List<Course> findByPriceBetween(Integer minPrice, Integer maxPrice);
    List<Course> findByPriceGreaterThanEqual(Integer minPrice);
    List<Course> findByPriceLessThanEqual(Integer maxPrice);

    // Фильтрация по продолжительности
    List<Course> findByDurationGreaterThanEqual(Integer minDuration);
    List<Course> findByDurationLessThanEqual(Integer maxDuration);

    // Фильтрация по активности
    List<Course> findByActive(Boolean active); // Исправлено имя поля
}