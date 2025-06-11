package com.course_api.demo.controller;


import com.course_api.demo.repository.course;
import com.course_api.demo.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<course> findAll() {
        return courseService.findAll();

    }
    @PostMapping
    public course create(@RequestBody course course){
        return courseService.create(course);
    }
    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable Long id){
        courseService.delete(id);
    }
}
