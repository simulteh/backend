package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.CourseDTO;
import com.simul_tech.netgenius.mappers.CourseMapper;
import com.simul_tech.netgenius.repositories.CourseRepository;
import com.simul_tech.netgenius.repositories.Course;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseDTO> findAll() {
        return courseRepository.findAll().stream()
                .map(CourseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO findById(Long id) {
        return courseRepository.findById(id)
                .map(CourseMapper::toDTO)
                .orElse(null);
    }

    public CourseDTO create(CourseDTO courseDTO) {
        Course entity = CourseMapper.toEntity(courseDTO);
        Course savedEntity = courseRepository.save(entity);
        return CourseMapper.toDTO(savedEntity);
    }

    public CourseDTO update(Long id, CourseDTO courseDTO) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Обновляем поля
        existingCourse.setTitle(courseDTO.getTitle());
        existingCourse.setDescription(courseDTO.getDescription());
        existingCourse.setPrice(courseDTO.getPrice());
        existingCourse.setDuration(courseDTO.getDuration());
        existingCourse.setActive(courseDTO.getActive());

        Course updatedCourse = courseRepository.save(existingCourse);
        return CourseMapper.toDTO(updatedCourse);
    }

    // ИСПРАВЛЕННЫЙ МЕТОД DELETE
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course with id: " + id + " not found");
        }
        courseRepository.deleteById(id);
    }

    // Методы фильтрации
    public List<CourseDTO> findByPriceBetween(Integer minPrice, Integer maxPrice) {
        return courseRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(CourseMapper::toDTO)
                .collect(Collectors.toList());
    }
    public  List<CourseDTO> findByPriceGreaterThanEqual(Integer minPrice){
        return courseRepository.findByPriceGreaterThanEqual(minPrice).stream().map(CourseMapper::toDTO).collect(Collectors.toList());
    }
    public List<CourseDTO> findByPriceLessThanEqual(Integer maxPrice){
        return courseRepository.findByPriceLessThanEqual(maxPrice).stream().map(CourseMapper::toDTO).collect(Collectors.toList());
    }
    public List<CourseDTO> findByDurationGreaterThanEqual(Integer minDuration){
        return courseRepository.findByDurationGreaterThanEqual(minDuration).stream().map(CourseMapper::toDTO).collect(Collectors.toList());
    }
    public  List<CourseDTO> findByDurationLessThanEqual(Integer maxDuration){
        return courseRepository.findByDurationLessThanEqual(maxDuration).stream().map(CourseMapper::toDTO).collect(Collectors.toList());

    }
    public List<CourseDTO> findByActive(Boolean active){
        return courseRepository.findByActive(active).stream().map(CourseMapper::toDTO).collect(Collectors.toList());
    }
}