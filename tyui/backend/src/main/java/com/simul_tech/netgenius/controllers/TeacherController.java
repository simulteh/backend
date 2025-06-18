package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.ApiResponse;
import com.simul_tech.netgenius.models.CreateTeacherRequest;
import com.simul_tech.netgenius.models.ErrorResponse;
import com.simul_tech.netgenius.models.TeacherDto;
import com.simul_tech.netgenius.models.TeacherFilterRequest;
import com.simul_tech.netgenius.services.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@Tag(name = "Преподаватели", description = "API для управления преподавателями")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {
    
    private final TeacherService teacherService;
    
    @PostMapping
    @Operation(
            summary = "Создание нового преподавателя",
            description = "Создает нового преподавателя с указанными данными. Email должен быть уникальным.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные преподавателя",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateTeacherRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример создания преподавателя",
                                    value = "{\"firstName\": \"Иван\", \"lastName\": \"Петров\", \"email\": \"ivan.petrov@university.edu\", \"phone\": \"+7-999-123-45-67\", \"specialization\": \"Математика\", \"department\": \"Факультет математики\", \"academicDegree\": \"Кандидат наук\", \"academicTitle\": \"Доцент\", \"experienceYears\": 10}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Преподаватель успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешное создание",
                                    value = "{\"status\": \"success\", \"message\": \"Преподаватель успешно создан\", \"data\": {\"id\": 1, \"firstName\": \"Иван\", \"lastName\": \"Петров\", \"email\": \"ivan.petrov@university.edu\"}, \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка валидации данных",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Преподаватель с таким email уже существует",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<TeacherDto>> createTeacher(@RequestBody CreateTeacherRequest request) {
        log.info("Попытка создания преподавателя: {} {}", request.getFirstName(), request.getLastName());
        
        try {
            TeacherDto createdTeacher = teacherService.createTeacher(request);
            log.info("Преподаватель успешно создан: {} {} (ID: {})", 
                    request.getFirstName(), request.getLastName(), createdTeacher.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Преподаватель успешно создан", createdTeacher));
        } catch (Exception e) {
            log.error("Ошибка при создании преподавателя {} {}: {}", 
                    request.getFirstName(), request.getLastName(), e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    @Operation(
            summary = "Получение преподавателя по ID",
            description = "Возвращает данные преподавателя по указанному ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Преподаватель найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Преподаватель найден",
                                    value = "{\"status\": \"success\", \"message\": \"Преподаватель найден\", \"data\": {\"id\": 1, \"firstName\": \"Иван\", \"lastName\": \"Петров\"}, \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Преподаватель не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<TeacherDto>> getTeacherById(
            @Parameter(description = "ID преподавателя", example = "1")
            @PathVariable Long id) {
        log.info("Запрос преподавателя по ID: {}", id);
        
        try {
            TeacherDto teacher = teacherService.getTeacherById(id);
            log.info("Преподаватель найден: {} {} (ID: {})", teacher.getFirstName(), teacher.getLastName(), id);
            return ResponseEntity.ok(ApiResponse.success("Преподаватель найден", teacher));
        } catch (Exception e) {
            log.error("Ошибка при получении преподавателя с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping
    @Operation(
            summary = "Получение всех преподавателей",
            description = "Возвращает список всех преподавателей в системе"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список преподавателей получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "Список преподавателей",
                            value = "{\"status\": \"success\", \"message\": \"Список преподавателей получен\", \"data\": [{\"id\": 1, \"firstName\": \"Иван\", \"lastName\": \"Петров\"}], \"timestamp\": \"2024-01-15T10:30:00\"}"
                    )))
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getAllTeachers() {
        log.info("Запрос всех преподавателей");
        
        try {
            List<TeacherDto> teachers = teacherService.getAllTeachers();
            log.info("Получено {} преподавателей", teachers.size());
            return ResponseEntity.ok(ApiResponse.success("Список преподавателей получен", teachers));
        } catch (Exception e) {
            log.error("Ошибка при получении всех преподавателей: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновление преподавателя",
            description = "Обновляет данные преподавателя по указанному ID. Email должен оставаться уникальным."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Преподаватель успешно обновлен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешное обновление",
                                    value = "{\"status\": \"success\", \"message\": \"Преподаватель успешно обновлен\", \"data\": {\"id\": 1, \"firstName\": \"Иван\", \"lastName\": \"Петров\"}, \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Преподаватель не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Преподаватель с таким email уже существует",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<TeacherDto>> updateTeacher(
            @Parameter(description = "ID преподавателя", example = "1")
            @PathVariable Long id, 
            @RequestBody CreateTeacherRequest request) {
        log.info("Попытка обновления преподавателя с ID {}: {} {}", id, request.getFirstName(), request.getLastName());
        
        try {
            TeacherDto updatedTeacher = teacherService.updateTeacher(id, request);
            log.info("Преподаватель успешно обновлен: {} {} (ID: {})", 
                    request.getFirstName(), request.getLastName(), id);
            return ResponseEntity.ok(ApiResponse.success("Преподаватель успешно обновлен", updatedTeacher));
        } catch (Exception e) {
            log.error("Ошибка при обновлении преподавателя с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удаление преподавателя",
            description = "Выполняет мягкое удаление преподавателя (устанавливает isActive = false)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Преподаватель успешно удален"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Преподаватель не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(
            @Parameter(description = "ID преподавателя", example = "1")
            @PathVariable Long id) {
        log.info("Попытка удаления преподавателя с ID: {}", id);
        
        try {
            teacherService.deleteTeacher(id);
            log.info("Преподаватель успешно удален (ID: {})", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success("Преподаватель успешно удален", null));
        } catch (Exception e) {
            log.error("Ошибка при удалении преподавателя с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @PostMapping("/filter")
    @Operation(
            summary = "Фильтрация преподавателей",
            description = "Фильтрует преподавателей по различным критериям с пагинацией и сортировкой",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Параметры фильтрации",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TeacherFilterRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример фильтрации",
                                    value = "{\"firstName\": \"Иван\", \"department\": \"Математика\", \"minExperienceYears\": 5, \"maxExperienceYears\": 15, \"isActive\": true, \"sortBy\": \"lastName\", \"sortDirection\": \"ASC\", \"page\": 0, \"size\": 10}"
                            )
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Отфильтрованные преподаватели получены",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "Результат фильтрации",
                            value = "{\"status\": \"success\", \"message\": \"Фильтрация завершена\", \"data\": {\"content\": [{\"id\": 1, \"firstName\": \"Иван\", \"lastName\": \"Петров\"}], \"totalElements\": 1, \"totalPages\": 1}, \"timestamp\": \"2024-01-15T10:30:00\"}"
                    )))
    public ResponseEntity<ApiResponse<Page<TeacherDto>>> filterTeachers(@RequestBody TeacherFilterRequest filterRequest) {
        log.info("Запрос фильтрации преподавателей: firstName={}, department={}, page={}, size={}", 
                filterRequest.getFirstName(), filterRequest.getDepartment(), filterRequest.getPage(), filterRequest.getSize());
        
        try {
            Page<TeacherDto> teachers = teacherService.filterTeachers(filterRequest);
            log.info("Фильтрация завершена: найдено {} преподавателей на странице {} из {}", 
                    teachers.getContent().size(), teachers.getNumber(), teachers.getTotalPages());
            return ResponseEntity.ok(ApiResponse.success("Фильтрация завершена", teachers));
        } catch (Exception e) {
            log.error("Ошибка при фильтрации преподавателей: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/department/{department}")
    @Operation(
            summary = "Получение преподавателей по отделению",
            description = "Возвращает список преподавателей указанного отделения"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список преподавателей отделения получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getTeachersByDepartment(
            @Parameter(description = "Название отделения", example = "Факультет математики")
            @PathVariable String department) {
        log.info("Запрос преподавателей отделения: {}", department);
        
        try {
            List<TeacherDto> teachers = teacherService.getTeachersByDepartment(department);
            log.info("Найдено {} преподавателей в отделении: {}", teachers.size(), department);
            return ResponseEntity.ok(ApiResponse.success("Список преподавателей отделения получен", teachers));
        } catch (Exception e) {
            log.error("Ошибка при получении преподавателей отделения {}: {}", department, e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/specialization/{specialization}")
    @Operation(
            summary = "Получение преподавателей по специализации",
            description = "Возвращает список преподавателей указанной специализации"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список преподавателей специализации получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getTeachersBySpecialization(
            @Parameter(description = "Специализация", example = "Математика")
            @PathVariable String specialization) {
        log.info("Запрос преподавателей специализации: {}", specialization);
        
        try {
            List<TeacherDto> teachers = teacherService.getTeachersBySpecialization(specialization);
            log.info("Найдено {} преподавателей специализации: {}", teachers.size(), specialization);
            return ResponseEntity.ok(ApiResponse.success("Список преподавателей специализации получен", teachers));
        } catch (Exception e) {
            log.error("Ошибка при получении преподавателей специализации {}: {}", specialization, e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/academic-degree/{academicDegree}")
    @Operation(
            summary = "Получение преподавателей по ученой степени",
            description = "Возвращает список преподавателей с указанной ученой степенью"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список преподавателей по ученой степени получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getTeachersByAcademicDegree(
            @Parameter(description = "Ученая степень", example = "Кандидат наук")
            @PathVariable String academicDegree) {
        log.info("Запрос преподавателей ученой степени: {}", academicDegree);
        
        try {
            List<TeacherDto> teachers = teacherService.getTeachersByAcademicDegree(academicDegree);
            log.info("Найдено {} преподавателей с ученой степенью: {}", teachers.size(), academicDegree);
            return ResponseEntity.ok(ApiResponse.success("Список преподавателей по ученой степени получен", teachers));
        } catch (Exception e) {
            log.error("Ошибка при получении преподавателей ученой степени {}: {}", academicDegree, e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/academic-title/{academicTitle}")
    @Operation(
            summary = "Получение преподавателей по ученому званию",
            description = "Возвращает список преподавателей с указанным ученым званием"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список преподавателей по ученому званию получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getTeachersByAcademicTitle(
            @Parameter(description = "Ученое звание", example = "Доцент")
            @PathVariable String academicTitle) {
        log.info("Запрос преподавателей ученого звания: {}", academicTitle);
        
        try {
            List<TeacherDto> teachers = teacherService.getTeachersByAcademicTitle(academicTitle);
            log.info("Найдено {} преподавателей с ученым званием: {}", teachers.size(), academicTitle);
            return ResponseEntity.ok(ApiResponse.success("Список преподавателей по ученому званию получен", teachers));
        } catch (Exception e) {
            log.error("Ошибка при получении преподавателей ученого звания {}: {}", academicTitle, e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/experience-range")
    @Operation(
            summary = "Получение преподавателей по диапазону опыта работы",
            description = "Возвращает список преподавателей с опытом работы в указанном диапазоне"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список преподавателей по опыту работы получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getTeachersByExperienceRange(
            @Parameter(description = "Минимальный опыт работы в годах", example = "5")
            @RequestParam Integer minYears, 
            @Parameter(description = "Максимальный опыт работы в годах", example = "15")
            @RequestParam Integer maxYears) {
        log.info("Запрос преподавателей по опыту работы: {} - {} лет", minYears, maxYears);
        
        try {
            List<TeacherDto> teachers = teacherService.getTeachersByExperienceRange(minYears, maxYears);
            log.info("Найдено {} преподавателей с опытом работы {} - {} лет", teachers.size(), minYears, maxYears);
            return ResponseEntity.ok(ApiResponse.success("Список преподавателей по опыту работы получен", teachers));
        } catch (Exception e) {
            log.error("Ошибка при получении преподавателей по опыту работы {} - {}: {}", minYears, maxYears, e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/active")
    @Operation(
            summary = "Получение активных преподавателей",
            description = "Возвращает список всех активных преподавателей (isActive = true)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список активных преподавателей получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getActiveTeachers() {
        log.info("Запрос активных преподавателей");
        
        try {
            List<TeacherDto> teachers = teacherService.getActiveTeachers();
            log.info("Найдено {} активных преподавателей", teachers.size());
            return ResponseEntity.ok(ApiResponse.success("Список активных преподавателей получен", teachers));
        } catch (Exception e) {
            log.error("Ошибка при получении активных преподавателей: {}", e.getMessage(), e);
            throw e;
        }
    }
} 