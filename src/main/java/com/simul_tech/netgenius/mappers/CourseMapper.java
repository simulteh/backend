package com.simul_tech.netgenius.mappers;

import com.simul_tech.netgenius.dto.CourseDTO;
import com.simul_tech.netgenius.repositories.Course;

public class CourseMapper {

    public static CourseDTO toDTO(Course entity) {
        CourseDTO dto = new CourseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setDuration(entity.getDuration());
        dto.setActive(entity.getActive()); // Исправлено
        return dto;
    }

    public static Course toEntity(CourseDTO dto) {
        Course entity = new Course();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setDuration(dto.getDuration());
        entity.setActive(dto.getActive()); // Исправлено
        return entity;
    }
}