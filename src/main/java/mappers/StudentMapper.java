package com.simul_tech.netgenius.mappers;

import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.models.StudentDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentMapper {

    /**
     * Преобразует сущность Student в StudentDTO
     * @param student сущность студента из базы данных
     * @return объект StudentDTO
     */
    public StudentDto toDto(Student student) {
        if (student == null) {
            return null;
        }

        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setFullName(student.getFullName());
        dto.setGroup(student.getGroup());
        dto.setEnrollmentDate(student.getEnrollmentDate());

        return dto;
    }

    /**
     * Преобразует StudentDTO в сущность Student
     * @param dto объект передачи данных
     * @return сущность Student
     */
    public Student toEntity(StudentDto dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        student.setId(dto.getId());
        student.setFullName(dto.getFullName());
        student.setGroup(dto.getGroup());
        student.setEnrollmentDate(dto.getEnrollmentDate());

        return student;
    }

    /**
     * Преобразует список сущностей Student в список StudentDTO
     * @param students список студентов из базы данных
     * @return список DTO объектов
     */
    public List<StudentDto> toDtoList(List<Student> students) {
        if (students == null) {
            return null;
        }

        return students.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Обновляет существующую сущность Student данными из StudentDTO
     * @param student сущность для обновления
     * @param dto объект с новыми данными
     */
    public void updateEntityFromDto(Student student, StudentDto dto) {
        if (student == null || dto == null) {
            return;
        }

        if (dto.getFullName() != null) {
            student.setFullName(dto.getFullName());
        }
        if (dto.getGroup() != null) {
            student.setGroup(dto.getGroup());
        }
        if (dto.getEnrollmentDate() != null) {
            student.setEnrollmentDate(dto.getEnrollmentDate());
        }
    }
}