package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.ApiResponse;
import com.simul_tech.netgenius.models.StudentDto;
import com.simul_tech.netgenius.models.ErrorResponse;
import com.simul_tech.netgenius.models.StudentFilterRequest;
import com.simul_tech.netgenius.services.StudentService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Студенты", description = "API для управления данными студентов")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @Operation(
            summary = "Добавление нового студента",
            description = "Создает нового студента с указанными данными.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные студента",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = StudentDto.class),
                            examples = @ExampleObject(
                                    name = "Пример создания студента",
                                    value = "{\"fullName\": \"Иванов Иван Иванович\", \"group\": \"ИТ-101\", \"enrollmentDate\": \"2023-09-01\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Студент успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешное создание",
                                    value = "{\"status\": \"success\", \"message\": \"Студент успешно создан\", \"data\": {\"id\": 1, \"fullName\": \"Иванов Иван Иванович\", \"group\": \"ИТ-101\"}, \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка валидации данных",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@RequestBody StudentDto request) {
        log.info("Попытка создания студента: {}", request.getFullName());

        try {
            StudentDto createdStudent = studentService.createStudent(request);
            log.info("Студент успешно создан: {} (ID: {})", request.getFullName(), createdStudent.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Студент успешно создан", createdStudent));
        } catch (Exception e) {
            log.error("Ошибка при создании студента {}: {}", request.getFullName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получение студента по ID",
            description = "Возвращает данные студента по указанному ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Студент найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Студент найден",
                                    value = "{\"status\": \"success\", \"message\": \"Студент найден\", \"data\": {\"id\": 1, \"fullName\": \"Иванов Иван Иванович\", \"group\": \"ИТ-101\"}, \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Студент не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<StudentDto>> getStudentById(
            @Parameter(description = "ID студента", example = "1")
            @PathVariable Long id) {
        log.info("Запрос студента по ID: {}", id);

        try {
            StudentDto student = studentService.getStudentById(id);
            log.info("Студент найден: {} (ID: {})", student.getFullName(), id);
            return ResponseEntity.ok(ApiResponse.success("Студент найден", student));
        } catch (Exception e) {
            log.error("Ошибка при получении студента с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    @Operation(
            summary = "Получение всех студентов",
            description = "Возвращает список всех студентов с возможностью фильтрации"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список студентов получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "Список студентов",
                            value = "{\"status\": \"success\", \"message\": \"Список студентов получен\", \"data\": [{\"id\": 1, \"fullName\": \"Иванов Иван Иванович\", \"group\": \"ИТ-101\"}], \"timestamp\": \"2024-01-15T10:30:00\"}"
                    )))
    public ResponseEntity<ApiResponse<List<StudentDto>>> getAllStudents(
            @Parameter(description = "ID студента", example = "1") @RequestParam(required = false) Long id,
            @Parameter(description = "Часть ФИО", example = "Иванов") @RequestParam(required = false) String fullName,
            @Parameter(description = "Группа", example = "ИТ-101") @RequestParam(required = false) String group,
            @Parameter(description = "Дата зачисления", example = "2023-09-01") @RequestParam(required = false) LocalDate enrollmentDate) {
        log.info("Запрос всех студентов с фильтрами: id={}, fullName={}, group={}, enrollmentDate={}",
                id, fullName, group, enrollmentDate);

        try {
            List<StudentDto> students = studentService.getAllStudents(id, fullName, group, enrollmentDate);
            log.info("Получено {} студентов", students.size());
            return ResponseEntity.ok(ApiResponse.success("Список студентов получен", students));
        } catch (Exception e) {
            log.error("Ошибка при получении всех студентов: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновление данных студента",
            description = "Обновляет данные студента по указанному ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Студент успешно обновлен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешное обновление",
                                    value = "{\"status\": \"success\", \"message\": \"Студент успешно обновлен\", \"data\": {\"id\": 1, \"fullName\": \"Иванов Иван Иванович\", \"group\": \"ИТ-101\"}, \"timestamp\": \"2024-01-15T10:30:00\"}"
                            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Студент не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<StudentDto>> updateStudent(
            @Parameter(description = "ID студента", example = "1")
            @PathVariable Long id,
            @RequestBody StudentDto request) {
        log.info("Попытка обновления студента с ID {}: {}", id, request.getFullName());

        try {
            StudentDto updatedStudent = studentService.updateStudent(id, request);
            log.info("Студент успешно обновлен: {} (ID: {})", request.getFullName(), id);
            return ResponseEntity.ok(ApiResponse.success("Студент успешно обновлен", updatedStudent));
        } catch (Exception e) {
            log.error("Ошибка при обновлении студента с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удаление студента",
            description = "Удаляет студента по указанному ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Студент успешно удален"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Студент не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @Parameter(description = "ID студента", example = "1")
            @PathVariable Long id) {
        log.info("Попытка удаления студента с ID: {}", id);

        try {
            studentService.deleteStudent(id);
            log.info("Студент успешно удален (ID: {})", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success("Студент успешно удален", null));
        } catch (Exception e) {
            log.error("Ошибка при удалении студента с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/filter/fullName")
    @Operation(
            summary = "Поиск студентов по части ФИО",
            description = "Возвращает список студентов, чье ФИО содержит указанную строку (без учета регистра)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список студентов получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<StudentDto>>> getStudentsByFullNameContaining(
            @Parameter(description = "Часть ФИО", example = "Иванов")
            @RequestParam String partialName) {
        log.info("Запрос студентов по части ФИО: {}", partialName);

        try {
            List<StudentDto> students = studentService.findByFullNameContainingIgnoreCase(partialName);
            log.info("Найдено {} студентов с ФИО, содержащим '{}'", students.size(), partialName);
            return ResponseEntity.ok(ApiResponse.success("Список студентов получен", students));
        } catch (Exception e) {
            log.error("Ошибка при поиске студентов по части ФИО {}: {}", partialName, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/filter/group")
    @Operation(
            summary = "Поиск студентов по группе",
            description = "Возвращает список студентов указанной группы или содержащей указанную строку"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список студентов получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<StudentDto>>> getStudentsByGroup(
            @Parameter(description = "Название группы или его часть", example = "ИТ-101")
            @RequestParam String group,
            @Parameter(description = "Точный поиск (true) или частичный (false)", example = "false")
            @RequestParam(defaultValue = "false") boolean exactMatch) {
        log.info("Запрос студентов по группе: {} (точный поиск: {})", group, exactMatch);

        try {
            List<StudentDto> students = exactMatch ?
                    studentService.findByGroup(group) :
                    studentService.findByGroupContaining(group);
            log.info("Найдено {} студентов в группе '{}'", students.size(), group);
            return ResponseEntity.ok(ApiResponse.success("Список студентов получен", students));
        } catch (Exception e) {
            log.error("Ошибка при поиске студентов по группе {}: {}", group, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/filter/enrollmentDate")
    @Operation(
            summary = "Поиск студентов по дате зачисления",
            description = "Возвращает список студентов по диапазону дат зачисления"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список студентов получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<StudentDto>>> getStudentsByEnrollmentDate(
            @Parameter(description = "Начальная дата (включительно)", example = "2023-09-01")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Конечная дата (включительно)", example = "2023-09-30")
            @RequestParam(required = false) LocalDate endDate) {
        log.info("Запрос студентов по дате зачисления: {} - {}", startDate, endDate);

        try {
            List<StudentDto> students;
            if (startDate != null && endDate != null) {
                students = studentService.findByEnrollmentDateBetween(startDate, endDate);
            } else if (startDate != null) {
                students = studentService.findByEnrollmentDateAfter(startDate);
            } else if (endDate != null) {
                students = studentService.findByEnrollmentDateBefore(endDate);
            } else {
                students = studentService.getAllStudents(null, null, null, null);
            }

            log.info("Найдено {} студентов по указанным датам зачисления", students.size());
            return ResponseEntity.ok(ApiResponse.success("Список студентов получен", students));
        } catch (Exception e) {
            log.error("Ошибка при поиске студентов по дате зачисления {} - {}: {}", startDate, endDate, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/filter/combined")
    @Operation(
            summary = "Комбинированный поиск студентов",
            description = "Возвращает список студентов по комбинации параметров"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список студентов получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<StudentDto>>> getStudentsByCombinedFilters(
            @Parameter(description = "Часть ФИО", example = "Иванов")
            @RequestParam(required = false) String fullName,
            @Parameter(description = "Группа", example = "ИТ-101")
            @RequestParam(required = false) String group,
            @Parameter(description = "Начальная дата зачисления", example = "2023-09-01")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Конечная дата зачисления", example = "2023-09-30")
            @RequestParam(required = false) LocalDate endDate) {
        log.info("Комбинированный запрос студентов: fullName={}, group={}, dates={}-{}",
                fullName, group, startDate, endDate);

        try {
            List<StudentDto> students;
            if (fullName != null && group != null && startDate != null && endDate != null) {
                students = studentService.findByFullNameContainingAndGroupAndEnrollmentDateBetween(
                        fullName, group, startDate, endDate);
            } else if (fullName != null && group != null) {
                students = studentService.findByFullNameAndGroup(fullName, group);
            } else if (fullName != null && startDate != null && endDate != null) {
                students = studentService.findByFullNameContainingAndEnrollmentDateBetween(
                        fullName, startDate, endDate);
            } else {
                students = studentService.getAllStudents(null, fullName, group, startDate);
            }

            log.info("Найдено {} студентов по комбинированным параметрам", students.size());
            return ResponseEntity.ok(ApiResponse.success("Список студентов получен", students));
        } catch (Exception e) {
            log.error("Ошибка при комбинированном поиске студентов: {}", e.getMessage(), e);
            throw e;
        }
    }
}