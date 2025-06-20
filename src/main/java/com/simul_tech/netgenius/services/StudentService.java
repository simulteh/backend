package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.StudentNotFoundException;
import com.simul_tech.netgenius.exceptions.DuplicateStudentException;
import com.simul_tech.netgenius.mappers.StudentMapper;
import com.simul_tech.netgenius.models.*;
import com.simul_tech.netgenius.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    /**
     * Создает нового студента
     */
    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {
        log.info("Создание студента: {}", studentDto.getFullName());

        // Проверка уникальности данных может быть добавлена при необходимости
        Student student = studentMapper.toEntity(studentDto);
        Student savedStudent = studentRepository.save(student);

        log.info("Студент создан с ID: {}", savedStudent.getId());
        return studentMapper.toDto(savedStudent);
    }

    /**
     * Получает студента по ID
     */
    public StudentDto getStudentById(Long id) {
        log.debug("Получение студента по ID: {}", id);

        return studentRepository.findById(id)
                .map(studentMapper::toDto)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    /**
     * Получает список всех студентов с возможностью фильтрации
     */
    public List<StudentDto> getAllStudents(Long id, String fullName, String group, LocalDate enrollmentDate) {
        log.debug("Получение списка студентов с фильтрами: id={}, fullName={}, group={}, enrollmentDate={}",
                id, fullName, group, enrollmentDate);

        List<Student> students = studentRepository.findAllStudentsWithFilters(id, fullName, group, enrollmentDate);
        return studentMapper.toDtoList(students);
    }

    /**
     * Обновляет данные студента
     */
    @Transactional
    public StudentDto updateStudent(Long id, StudentDto studentDto) {
        log.info("Обновление студента с ID: {}", id);

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        studentMapper.updateEntityFromDto(existingStudent, studentDto);
        Student updatedStudent = studentRepository.save(existingStudent);

        log.info("Данные студента обновлены: ID {}", id);
        return studentMapper.toDto(updatedStudent);
    }

    /**
     * Удаляет студента
     */
    @Transactional
    public void deleteStudent(Long id) {
        log.info("Удаление студента с ID: {}", id);

        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException(id);
        }

        studentRepository.deleteById(id);
        log.info("Студент удален: ID {}", id);
    }

    /**
     * Фильтрация студентов с пагинацией
     */
    public Page<StudentDto> filterStudents(StudentFilterRequest filterRequest) {
        log.debug("Фильтрация студентов: {}", filterRequest);

        Sort sort = Sort.by(
                filterRequest.getSortDirection().equalsIgnoreCase("DESC") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filterRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                sort
        );

        Page<Student> studentsPage = studentRepository.findByFilters(filterRequest, pageable);
        return studentsPage.map(studentMapper::toDto);
    }

    /**
     * Поиск студентов по части ФИО (без учета регистра)
     */
    public List<StudentDto> findByFullNameContaining(String partialName) {
        log.debug("Поиск студентов по части ФИО: {}", partialName);
        return studentMapper.toDtoList(
                studentRepository.findByFullNameContainingIgnoreCase(partialName)
        );
    }

    /**
     * Поиск студентов по группе
     */
    public List<StudentDto> findByGroup(String group) {
        log.debug("Поиск студентов по группе: {}", group);
        return studentMapper.toDtoList(
                studentRepository.findByGroup(group)
        );
    }

    /**
     * Поиск студентов по диапазону дат зачисления
     */
    public List<StudentDto> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Поиск студентов по датам зачисления: {} - {}", startDate, endDate);
        return studentMapper.toDtoList(
                studentRepository.findByEnrollmentDateBetween(startDate, endDate)
        );
    }
}