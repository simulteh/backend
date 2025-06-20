package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setFullName(studentDetails.getFullName());
        student.setGroup(studentDetails.getGroup());
        student.setEnrollmentDate(studentDetails.getEnrollmentDate());

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public List<Student> filterStudents(String fullName, String group, LocalDate startDate, LocalDate endDate) {
        if (fullName != null && group != null) {
            return studentRepository.findByFullNameAndGroup(fullName, group);
        } else if (fullName != null) {
            return studentRepository.findByFullNameContainingIgnoreCase(fullName);
        } else if (group != null) {
            return studentRepository.findByGroup(group);
        } else if (startDate != null && endDate != null) {
            return studentRepository.findByEnrollmentDateBetween(startDate, endDate);
        }
        return studentRepository.findAll();
    }
}