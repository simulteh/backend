package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.mappers.TeacherMapper;
import com.simul_tech.netgenius.models.CreateTeacherRequest;
import com.simul_tech.netgenius.models.Teacher;
import com.simul_tech.netgenius.models.TeacherDto;
import com.simul_tech.netgenius.models.TeacherFilterRequest;
import com.simul_tech.netgenius.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {
    
    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    
    /**
     * Создает нового преподавателя
     */
    public TeacherDto createTeacher(CreateTeacherRequest request) {
        log.debug("Начало создания преподавателя: {} {}", request.getFirstName(), request.getLastName());
        
        if (teacherRepository.existsByEmail(request.getEmail())) {
            log.warn("Попытка создания преподавателя с существующим email: {}", request.getEmail());
            throw new RuntimeException("Преподаватель с таким email уже существует");
        }
        
        Teacher teacher = teacherMapper.toEntity(request);
        Teacher savedTeacher = teacherRepository.save(teacher);
        
        log.debug("Преподаватель создан в базе данных с ID: {}", savedTeacher.getId());
        return teacherMapper.toDto(savedTeacher);
    }
    
    /**
     * Получает преподавателя по ID
     */
    public TeacherDto getTeacherById(Long id) {
        log.debug("Поиск преподавателя по ID: {}", id);
        
        Optional<Teacher> teacher = teacherRepository.findById(id);
        if (teacher.isEmpty()) {
            log.warn("Преподаватель с ID {} не найден", id);
            throw new RuntimeException("Преподаватель не найден");
        }
        
        log.debug("Преподаватель найден: {} {} (ID: {})", 
                teacher.get().getFirstName(), teacher.get().getLastName(), id);
        return teacherMapper.toDto(teacher.get());
    }
    
    /**
     * Получает всех преподавателей
     */
    public List<TeacherDto> getAllTeachers() {
        log.debug("Запрос всех преподавателей из базы данных");
        
        List<Teacher> teachers = teacherRepository.findAll();
        log.debug("Найдено {} преподавателей в базе данных", teachers.size());
        
        return teacherMapper.toDtoList(teachers);
    }
    
    /**
     * Обновляет преподавателя
     */
    public TeacherDto updateTeacher(Long id, CreateTeacherRequest request) {
        log.debug("Начало обновления преподавателя с ID {}: {} {}", 
                id, request.getFirstName(), request.getLastName());
        
        Optional<Teacher> teacherOpt = teacherRepository.findById(id);
        if (teacherOpt.isEmpty()) {
            log.warn("Попытка обновления несуществующего преподавателя с ID: {}", id);
            throw new RuntimeException("Преподаватель не найден");
        }
        
        Teacher teacher = teacherOpt.get();
        
        // Проверяем, что email не занят другим преподавателем
        if (!teacher.getEmail().equals(request.getEmail()) && 
            teacherRepository.existsByEmail(request.getEmail())) {
            log.warn("Попытка обновления преподавателя с существующим email: {}", request.getEmail());
            throw new RuntimeException("Преподаватель с таким email уже существует");
        }
        
        teacherMapper.updateEntityFromRequest(teacher, request);
        Teacher updatedTeacher = teacherRepository.save(teacher);
        
        log.debug("Преподаватель обновлен в базе данных: {} {} (ID: {})", 
                updatedTeacher.getFirstName(), updatedTeacher.getLastName(), id);
        return teacherMapper.toDto(updatedTeacher);
    }
    
    /**
     * Удаляет преподавателя (мягкое удаление)
     */
    public void deleteTeacher(Long id) {
        log.debug("Начало мягкого удаления преподавателя с ID: {}", id);
        
        Optional<Teacher> teacherOpt = teacherRepository.findById(id);
        if (teacherOpt.isEmpty()) {
            log.warn("Попытка удаления несуществующего преподавателя с ID: {}", id);
            throw new RuntimeException("Преподаватель не найден");
        }
        
        Teacher teacher = teacherOpt.get();
        teacher.setIsActive(false);
        teacherRepository.save(teacher);
        
        log.debug("Преподаватель помечен как неактивный: {} {} (ID: {})", 
                teacher.getFirstName(), teacher.getLastName(), id);
    }
    
    /**
     * Фильтрует преподавателей по критериям
     */
    public Page<TeacherDto> filterTeachers(TeacherFilterRequest filterRequest) {
        log.debug("Начало фильтрации преподавателей с параметрами: firstName={}, department={}, page={}, size={}", 
                filterRequest.getFirstName(), filterRequest.getDepartment(), filterRequest.getPage(), filterRequest.getSize());
        
        // Создаем объект для пагинации и сортировки
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
        
        // Выполняем фильтрацию
        Page<Teacher> teachersPage = teacherRepository.findTeachersByFilters(
            filterRequest.getFirstName(),
            filterRequest.getLastName(),
            filterRequest.getEmail(),
            filterRequest.getSpecialization(),
            filterRequest.getDepartment(),
            filterRequest.getAcademicDegree(),
            filterRequest.getAcademicTitle(),
            filterRequest.getMinExperienceYears(),
            filterRequest.getMaxExperienceYears(),
            filterRequest.getIsActive(),
            pageable
        );
        
        log.debug("Фильтрация завершена: найдено {} преподавателей на странице {} из {}", 
                teachersPage.getContent().size(), teachersPage.getNumber(), teachersPage.getTotalPages());
        
        // Преобразуем в DTO
        return teachersPage.map(teacherMapper::toDto);
    }
    
    /**
     * Получает преподавателей по отделению
     */
    public List<TeacherDto> getTeachersByDepartment(String department) {
        log.debug("Поиск преподавателей по отделению: {}", department);
        
        List<Teacher> teachers = teacherRepository.findByDepartment(department);
        log.debug("Найдено {} преподавателей в отделении: {}", teachers.size(), department);
        
        return teacherMapper.toDtoList(teachers);
    }
    
    /**
     * Получает преподавателей по специализации
     */
    public List<TeacherDto> getTeachersBySpecialization(String specialization) {
        log.debug("Поиск преподавателей по специализации: {}", specialization);
        
        List<Teacher> teachers = teacherRepository.findBySpecialization(specialization);
        log.debug("Найдено {} преподавателей по специализации: {}", teachers.size(), specialization);
        
        return teacherMapper.toDtoList(teachers);
    }
    
    /**
     * Получает преподавателей по ученой степени
     */
    public List<TeacherDto> getTeachersByAcademicDegree(String academicDegree) {
        log.debug("Поиск преподавателей по ученой степени: {}", academicDegree);
        
        List<Teacher> teachers = teacherRepository.findByAcademicDegree(academicDegree);
        log.debug("Найдено {} преподавателей с ученой степенью: {}", teachers.size(), academicDegree);
        
        return teacherMapper.toDtoList(teachers);
    }
    
    /**
     * Получает преподавателей по ученому званию
     */
    public List<TeacherDto> getTeachersByAcademicTitle(String academicTitle) {
        log.debug("Поиск преподавателей по ученому званию: {}", academicTitle);
        
        List<Teacher> teachers = teacherRepository.findByAcademicTitle(academicTitle);
        log.debug("Найдено {} преподавателей с ученым званием: {}", teachers.size(), academicTitle);
        
        return teacherMapper.toDtoList(teachers);
    }
    
    /**
     * Получает преподавателей по диапазону опыта работы
     */
    public List<TeacherDto> getTeachersByExperienceRange(Integer minYears, Integer maxYears) {
        log.debug("Поиск преподавателей по диапазону опыта: {} - {} лет", minYears, maxYears);
        
        List<Teacher> teachers = teacherRepository.findByExperienceYearsBetween(minYears, maxYears);
        log.debug("Найдено {} преподавателей с опытом работы от {} до {} лет", teachers.size(), minYears, maxYears);
        
        return teacherMapper.toDtoList(teachers);
    }
    
    /**
     * Получает активных преподавателей
     */
    public List<TeacherDto> getActiveTeachers() {
        log.debug("Поиск активных преподавателей");
        
        List<Teacher> teachers = teacherRepository.findByIsActive(true);
        log.debug("Найдено {} активных преподавателей", teachers.size());
        
        return teacherMapper.toDtoList(teachers);
    }
} 