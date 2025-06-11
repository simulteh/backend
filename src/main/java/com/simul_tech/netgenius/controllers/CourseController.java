package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.CourseDTO;
import com.simul_tech.netgenius.services.CourseService;
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
    public List<CourseDTO> findAll() {
        return courseService.findAll();
    }

    @GetMapping("/{id}")
    public CourseDTO findById(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @PostMapping
    public CourseDTO create(@RequestBody CourseDTO courseDTO) {
        return courseService.create(courseDTO);
    }

    @PutMapping("/{id}")
    public CourseDTO update(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        return courseService.update(id, courseDTO);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    // Эндпоинты для фильтрации
    @GetMapping("/filter/price")
    public List<CourseDTO> findByPriceBetween(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice) {
        return courseService.findByPriceBetween(minPrice, maxPrice);
    }

    // Добавить остальные фильтры аналогично
}