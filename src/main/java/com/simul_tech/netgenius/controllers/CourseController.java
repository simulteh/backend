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

    // Фильтрация по цене
    @GetMapping("/filter/price/between")
    public List<CourseDTO> findByPriceBetween(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice) {
        return courseService.findByPriceBetween(minPrice, maxPrice);
    }

    @GetMapping("/filter/price/min")
    public List<CourseDTO> findByPriceGreaterThanEqual(
            @RequestParam Integer minPrice) {
        return courseService.findByPriceGreaterThanEqual(minPrice);
    }

    @GetMapping("/filter/price/max")
    public List<CourseDTO> findByPriceLessThanEqual(
            @RequestParam Integer maxPrice) {
        return courseService.findByPriceLessThanEqual(maxPrice);
    }

    // Фильтрация по продолжительности
    @GetMapping("/filter/duration/min")
    public List<CourseDTO> findByDurationGreaterThanEqual(
            @RequestParam Integer minDuration) {
        return courseService.findByDurationGreaterThanEqual(minDuration);
    }

    @GetMapping("/filter/duration/max")
    public List<CourseDTO> findByDurationLessThanEqual(
            @RequestParam Integer maxDuration) {
        return courseService.findByDurationLessThanEqual(maxDuration);
    }

    // Фильтрация по активности
    @GetMapping("/filter/active")
    public List<CourseDTO> findByActive(
            @RequestParam Boolean active) {
        return courseService.findByActive(active);
    }
}