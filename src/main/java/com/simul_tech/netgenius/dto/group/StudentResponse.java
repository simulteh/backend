package com.simul_tech.netgenius.dto.group;

import com.simul_tech.netgenius.models.Student;
import io.swagger.v3.oas.annotations.media.Schema;

/** Краткий профиль студента в составе групп преподавателя. */
@Schema(description = "Студент")
public record StudentResponse(
        Long studentId,
        String fullName,
        Long universityId,
        Integer course
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFullName(),
                student.getUniversityId(),
                student.getCourse()
        );
    }
}
