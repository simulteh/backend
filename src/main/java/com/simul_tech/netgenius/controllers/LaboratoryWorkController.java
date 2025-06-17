package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.LaboratoryWork;
import com.simul_tech.netgenius.models.LaboratoryWorkUpdateRequest;
import com.simul_tech.netgenius.models.LaboratoryWorkDto;
import com.simul_tech.netgenius.models.LaboratoryWorkMapper;
import com.simul_tech.netgenius.services.LaboratoryWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/laboratory_works")
@Tag(
    name = "Laboratory Works",
    description = "API для управления лабораторными работами. " +
                 "Позволяет создавать, просматривать, обновлять и удалять лабораторные работы. " +
                 "Поддерживает фильтрацию по различным параметрам и загрузку файлов."
)
@SecurityRequirement(name = "bearerAuth")
public class LaboratoryWorkController {

    private final LaboratoryWorkService laboratoryWorkService;

    @Autowired
    public LaboratoryWorkController(LaboratoryWorkService laboratoryWorkService) {
        this.laboratoryWorkService = laboratoryWorkService;
    }

    @GetMapping
    @Operation(
        summary = "Получить все работы",
        description = "Возвращает список всех лабораторных работ с возможностью фильтрации. " +
                     "Поддерживает фильтрацию по статусу, дате, студенту, преподавателю, типу файла, оценке и статусу закрытия.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешно получен список работ",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LaboratoryWorkDto.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Требуется авторизация",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет доступа",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<List<LaboratoryWorkDto>> getAllWorks(
        @Parameter(description = "Статус работы (completed, in_progress, graded)") 
        @RequestParam(required = false) String status,
        
        @Parameter(description = "Дата начала в формате ISO-8601 (например, 2024-01-01T00:00:00)") 
        @RequestParam(required = false) String startDate,
        
        @Parameter(description = "Дата окончания в формате ISO-8601 (например, 2024-12-31T23:59:59)") 
        @RequestParam(required = false) String endDate,
        
        @Parameter(description = "ID студента для фильтрации работ") 
        @RequestParam(required = false) Long idUser,
        
        @Parameter(description = "ID преподавателя для фильтрации работ") 
        @RequestParam(required = false) Long idRecipient,
        
        @Parameter(description = "Тип файла (pdf, doc, docx, xlsx, txt, png, jpeg, jpg)") 
        @RequestParam(required = false) String fileType,
        
        @Parameter(description = "Оценка за работу") 
        @RequestParam(required = false) Integer grade,
        
        @Parameter(description = "Флаг закрытия комментариев") 
        @RequestParam(required = false) Boolean isClosed
    ) {
        return ResponseEntity.ok(laboratoryWorkService.getAllWorksDto(status, startDate, endDate, idUser, idRecipient, fileType, grade, isClosed));
    }

    @GetMapping("/{id_work}")
    @Operation(
        summary = "Получить работу по ID",
        description = "Возвращает лабораторную работу по указанному идентификатору. " +
                     "Включает все данные о работе, кроме служебных полей (id_time, file_path).",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Работа найдена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LaboratoryWorkDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Работа не найдена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Требуется авторизация",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<LaboratoryWorkDto> getWorkById(
        @Parameter(description = "ID лабораторной работы", required = true) 
        @PathVariable Long id_work
    ) {
        return ResponseEntity.ok(LaboratoryWorkMapper.toDto(laboratoryWorkService.getWorkById(id_work)));
    }

    @GetMapping("/users/{id_user}/laboratory_works")
    @Operation(
        summary = "Получить все работы студента",
        description = "Возвращает список всех лабораторных работ конкретного студента. " +
                     "Включает работы во всех статусах и с разными оценками.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешно получен список работ",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LaboratoryWorkDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Студент не найден",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Требуется авторизация",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<List<LaboratoryWorkDto>> getStudentWorks(
        @Parameter(description = "ID студента", required = true) 
        @PathVariable Long id_user
    ) {
        return ResponseEntity.ok(laboratoryWorkService.getStudentWorks(id_user)
                .stream()
                .map(LaboratoryWorkMapper::toDto)
                .toList());
    }

    @PostMapping
    @Operation(
        summary = "Создать новую работу",
        description = "Создает новую лабораторную работу с загрузкой файла и комментарием. " +
                     "Поддерживает файлы до 100MB следующих типов: pdf, doc, docx, xlsx, txt, png, jpeg, jpg. " +
                     "Комментарий ограничен 1000 символами. " +
                     "Один студент может иметь не более 20 работ.",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Работа успешно создана",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LaboratoryWorkDto.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Неверные данные (превышен размер файла, неверный тип файла, превышен лимит работ)",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Требуется авторизация",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<LaboratoryWorkDto> createWork(
        @Parameter(description = "Файл работы (макс. 100MB)", required = true) 
        @RequestParam("file") MultipartFile file,
        
        @Parameter(description = "Комментарий к работе (макс. 1000 символов)") 
        @RequestParam(required = false) String comment,
        
        @Parameter(description = "ID получателя (преподавателя)", required = true) 
        @RequestParam Long recipientId
    ) throws IOException {
        // Validate comment length
        if (comment != null && comment.length() > 1000) {
            return ResponseEntity.badRequest().build();
        }

        // Validate file size (100MB)
        if (file.getSize() > 100 * 1024 * 1024) {
            return ResponseEntity.badRequest().build();
        }

        // TODO: Get current user ID from security context
        Long currentUserId = 1L; // Temporary hardcoded value
        return ResponseEntity.status(201)
                .body(LaboratoryWorkMapper.toDto(
                    laboratoryWorkService.createWork(file, comment, currentUserId, recipientId)
                ));
    }

    @PutMapping("/{id_work}")
    @Operation(
        summary = "Обновить данные работы",
        description = "Обновляет данные лабораторной работы (статус, оценка, закрытие комментариев). " +
                     "Преподаватели могут выставлять оценки и закрывать комментарии. " +
                     "Студенты могут только обновлять статус своих работ.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Работа успешно обновлена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LaboratoryWorkDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Работа не найдена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Требуется авторизация",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на обновление работы",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<LaboratoryWorkDto> updateWork(
        @Parameter(description = "ID лабораторной работы", required = true) 
        @PathVariable Long id_work,
        
        @Parameter(description = "Данные для обновления", required = true) 
        @RequestBody LaboratoryWorkUpdateRequest updateRequest
    ) {
        return ResponseEntity.ok(LaboratoryWorkMapper.toDto(
            laboratoryWorkService.updateWork(id_work, updateRequest)
        ));
    }

    @DeleteMapping("/{id_work}")
    @Operation(
        summary = "Удалить работу по ID",
        description = "Удаляет лабораторную работу по указанному идентификатору. " +
                     "Удаляет как запись из базы данных, так и файл из хранилища.",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Работа успешно удалена"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Работа не найдена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Требуется авторизация",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на удаление работы",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Void> deleteWork(
        @Parameter(description = "ID лабораторной работы", required = true) 
        @PathVariable Long id_work
    ) {
        laboratoryWorkService.deleteWork(id_work);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id_user}/laboratory_works")
    @Operation(
        summary = "Удалить все работы студента",
        description = "Удаляет все лабораторные работы конкретного студента. " +
                     "Удаляет как записи из базы данных, так и файлы из хранилища.",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Работы успешно удалены"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Студент не найден",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Требуется авторизация",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на удаление работ",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Void> deleteAllStudentWorks(
        @Parameter(description = "ID студента", required = true) 
        @PathVariable Long id_user
    ) {
        laboratoryWorkService.deleteAllStudentWorks(id_user);
        return ResponseEntity.noContent().build();
    }
} 