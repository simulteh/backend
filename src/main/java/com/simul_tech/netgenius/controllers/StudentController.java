package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.services.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "Create new student")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return new ResponseEntity<>(studentService.createStudent(student), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all/filtered students")
    public ResponseEntity<List<Student>> getStudents(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(studentService.filterStudents(fullName, group, startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        return ResponseEntity.ok(studentService.updateStudent(id, student));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}