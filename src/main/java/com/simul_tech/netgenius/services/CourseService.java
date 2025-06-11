package com.course_api.demo.service;

import com.course_api.demo.repository.CourseRepository;
import com.course_api.demo.repository.course;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<course> findAll() {
        return courseRepository.findAll();
    }
    public course create(course course){
        return courseRepository.save(course);
    }
    public void delete(long id){
        Optional<course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isPresent()) {
            throw new IllegalStateException("Course with id: " + id + " already exists");
        }
        courseRepository.deleteById(id);
    }
}
