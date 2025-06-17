package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.LaboratoryWork;
import com.simul_tech.netgenius.models.LaboratoryWorkUpdateRequest;
import com.simul_tech.netgenius.models.LaboratoryWorkDto;
import com.simul_tech.netgenius.services.LaboratoryWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/laboratory_works")
@Tag(name = "Laboratory Works", description = "API для управления лабораторными работами")
public class LaboratoryWorkController {

    private final LaboratoryWorkService laboratoryWorkService;

    @Autowired
    public LaboratoryWorkController(LaboratoryWorkService laboratoryWorkService) {
        this.laboratoryWorkService = laboratoryWorkService;
    }

    @GetMapping
    @Operation(
        summary = "Получить все работы",
        description = "Возвращает список всех лабораторных работ с возможностью фильтрации",
        responses = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список работ"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
        }
    )
    public ResponseEntity<List<LaboratoryWorkDto>> getAllWorks(
        @Parameter(description = "Статус работы") @RequestParam(required = false) String status,
        @Parameter(description = "Дата начала") @RequestParam(required = false) String startDate,
        @Parameter(description = "Дата окончания") @RequestParam(required = false) String endDate,
        @Parameter(description = "ID студента") @RequestParam(required = false) Long idUser,
        @Parameter(description = "ID преподавателя") @RequestParam(required = false) Long idRecipient,
        @Parameter(description = "Тип файла") @RequestParam(required = false) String fileType,
        @Parameter(description = "Оценка") @RequestParam(required = false) Integer grade,
        @Parameter(description = "Закрыта ли работа") @RequestParam(required = false) Boolean isClosed
    ) {
        return ResponseEntity.ok(laboratoryWorkService.getAllWorksDto(status, startDate, endDate, idUser, idRecipient, fileType, grade, isClosed));
    }

    @GetMapping("/{id_work}")
    @Operation(
        summary = "Получить работу по ID",
        description = "Возвращает лабораторную работу по указанному идентификатору",
        responses = {
            @ApiResponse(responseCode = "200", description = "Работа найдена"),
            @ApiResponse(responseCode = "404", description = "Работа не найдена"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
        }
    )
    public ResponseEntity<LaboratoryWork> getWorkById(
        @Parameter(description = "ID лабораторной работы") @PathVariable Long id_work
    ) {
        return ResponseEntity.ok(laboratoryWorkService.getWorkById(id_work));
    }

    @GetMapping("/users/{id_user}/laboratory_works")
    @Operation(
        summary = "Получить все работы студента",
        description = "Возвращает список всех лабораторных работ конкретного студента",
        responses = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список работ"),
            @ApiResponse(responseCode = "404", description = "Студент не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
        }
    )
    public ResponseEntity<List<LaboratoryWork>> getStudentWorks(
        @Parameter(description = "ID студента") @PathVariable Long id_user
    ) {
        return ResponseEntity.ok(laboratoryWorkService.getStudentWorks(id_user));
    }

    @PostMapping
    @Operation(
        summary = "Создать новую работу",
        description = "Создает новую лабораторную работу с загрузкой файла и комментарием",
        responses = {
            @ApiResponse(responseCode = "201", description = "Работа успешно создана"),
            @ApiResponse(responseCode = "400", description = "Неверные данные"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
        }
    )
    public ResponseEntity<LaboratoryWork> createWork(
        @Parameter(description = "Файл работы") @RequestParam("file") MultipartFile file,
        @Parameter(description = "Комментарий к работе") @RequestParam(required = false) String comment,
        @Parameter(description = "ID получателя (преподавателя)") @RequestParam Long recipientId
    ) throws IOException {
        // TODO: Get current user ID from security context
        Long currentUserId = 1L; // Temporary hardcoded value
        return ResponseEntity.status(201).body(laboratoryWorkService.createWork(file, comment, currentUserId, recipientId));
    }

    @PutMapping("/{id_work}")
    @Operation(
        summary = "Обновить данные работы",
        description = "Обновляет данные лабораторной работы (статус, оценка и т.д.)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Работа успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Работа не найдена"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
        }
    )
    public ResponseEntity<LaboratoryWork> updateWork(
        @Parameter(description = "ID лабораторной работы") @PathVariable Long id_work,
        @Parameter(description = "Данные для обновления") @RequestBody LaboratoryWorkUpdateRequest updateRequest
    ) {
        return ResponseEntity.ok(laboratoryWorkService.updateWork(id_work, updateRequest));
    }

    @DeleteMapping("/{id_work}")
    @Operation(
        summary = "Удалить работу по ID",
        description = "Удаляет лабораторную работу по указанному идентификатору",
        responses = {
            @ApiResponse(responseCode = "204", description = "Работа успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Работа не найдена"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
        }
    )
    public ResponseEntity<Void> deleteWork(
        @Parameter(description = "ID лабораторной работы") @PathVariable Long id_work
    ) {
        laboratoryWorkService.deleteWork(id_work);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id_user}/laboratory_works")
    @Operation(
        summary = "Удалить все работы студента",
        description = "Удаляет все лабораторные работы конкретного студента",
        responses = {
            @ApiResponse(responseCode = "204", description = "Работы успешно удалены"),
            @ApiResponse(responseCode = "404", description = "Студент не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация")
        }
    )
    public ResponseEntity<Void> deleteAllStudentWorks(
        @Parameter(description = "ID студента") @PathVariable Long id_user
    ) {
        laboratoryWorkService.deleteAllStudentWorks(id_user);
        return ResponseEntity.noContent().build();
    }
} 