package com.simul_tech.netgenius.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialization;
    private String department;
    private String academicDegree;
    private String academicTitle;
    private Integer experienceYears;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 