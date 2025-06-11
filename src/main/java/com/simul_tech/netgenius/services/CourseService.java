package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.repositories.CourseRepository;
import com.simul_tech.netgenius.repositories.course;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<com.simul_tech.netgenius.repositories.course> findAll() {
        return courseRepository.findAll();
    }
    public com.simul_tech.netgenius.repositories.course create(course course){

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
