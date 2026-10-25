package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.common.ApiError;
import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.group.CabinetGroupDtos.*;
import com.simul_tech.netgenius.services.CabinetGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cabinet Groups", description = "Группы личных кабинетов; studentId всегда равен users.id")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Требуется access-токен", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Группа, приглашение или пользователь не найдены", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Пользователь не является студентом или группа принадлежит другому вузу", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class CabinetGroupController {
    private final CabinetGroupService service;

    @GetMapping
    @Operation(summary = "Группы текущего преподавателя")
    public List<Summary> list() { return service.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать группу (TEACHER)")
    public Summary create(@Valid @RequestBody GroupRequest request) { return service.create(request); }

    @GetMapping("/{id}")
    @Operation(summary = "Состав своей группы, код приглашения и лучшие оценки студентов")
    public Detail detail(@PathVariable Long id) { return service.detail(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить название и организацию своей группы")
    public Summary update(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/students")
    @Operation(summary = "Добавить студента по ID пользователя; повторное добавление идемпотентно")
    public MessageResponse add(@PathVariable Long id, @Valid @RequestBody AddStudentRequest request) {
        return service.addStudent(id, request.studentId());
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @Operation(summary = "Удалить студента по ID пользователя из своей группы")
    public MessageResponse remove(@PathVariable Long id, @PathVariable Long studentId) {
        return service.removeStudent(id, studentId);
    }

    @PostMapping("/join/{inviteCode}")
    @Operation(summary = "Вступить в группу по коду (STUDENT); повторное вступление идемпотентно")
    public MyGroup join(@PathVariable String inviteCode) { return service.join(inviteCode); }

    @GetMapping("/my")
    @Operation(summary = "Группы текущего студента с ФИО преподавателя (STUDENT)")
    public List<MyGroup> my() { return service.myGroups(); }
}
