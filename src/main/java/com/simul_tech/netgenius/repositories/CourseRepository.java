package com.course_api.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<course,Long> {
}
