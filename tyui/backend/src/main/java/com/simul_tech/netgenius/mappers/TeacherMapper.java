package com.simul_tech.netgenius.mappers;

import com.simul_tech.netgenius.models.CreateTeacherRequest;
import com.simul_tech.netgenius.models.Teacher;
import com.simul_tech.netgenius.models.TeacherDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeacherMapper {
    
    /**
     * Преобразует сущность Teacher в TeacherDto
     */
    public TeacherDto toDto(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        
        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setFirstName(teacher.getFirstName());
        dto.setLastName(teacher.getLastName());
        dto.setEmail(teacher.getEmail());
        dto.setPhone(teacher.getPhone());
        dto.setSpecialization(teacher.getSpecialization());
        dto.setDepartment(teacher.getDepartment());
        dto.setAcademicDegree(teacher.getAcademicDegree());
        dto.setAcademicTitle(teacher.getAcademicTitle());
        dto.setExperienceYears(teacher.getExperienceYears());
        dto.setIsActive(teacher.getIsActive());
        dto.setCreatedAt(teacher.getCreatedAt());
        dto.setUpdatedAt(teacher.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * Преобразует CreateTeacherRequest в сущность Teacher
     */
    public Teacher toEntity(CreateTeacherRequest request) {
        if (request == null) {
            return null;
        }
        
        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setSpecialization(request.getSpecialization());
        teacher.setDepartment(request.getDepartment());
        teacher.setAcademicDegree(request.getAcademicDegree());
        teacher.setAcademicTitle(request.getAcademicTitle());
        teacher.setExperienceYears(request.getExperienceYears());
        teacher.setIsActive(true);
        
        return teacher;
    }
    
    /**
     * Преобразует список сущностей Teacher в список TeacherDto
     */
    public List<TeacherDto> toDtoList(List<Teacher> teachers) {
        if (teachers == null) {
            return null;
        }
        
        return teachers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Обновляет существующую сущность Teacher данными из CreateTeacherRequest
     */
    public void updateEntityFromRequest(Teacher teacher, CreateTeacherRequest request) {
        if (teacher == null || request == null) {
            return;
        }
        
        if (request.getFirstName() != null) {
            teacher.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            teacher.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            teacher.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }
        if (request.getSpecialization() != null) {
            teacher.setSpecialization(request.getSpecialization());
        }
        if (request.getDepartment() != null) {
            teacher.setDepartment(request.getDepartment());
        }
        if (request.getAcademicDegree() != null) {
            teacher.setAcademicDegree(request.getAcademicDegree());
        }
        if (request.getAcademicTitle() != null) {
            teacher.setAcademicTitle(request.getAcademicTitle());
        }
        if (request.getExperienceYears() != null) {
            teacher.setExperienceYears(request.getExperienceYears());
        }
    }
} 