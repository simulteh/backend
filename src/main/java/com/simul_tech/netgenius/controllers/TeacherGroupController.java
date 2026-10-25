package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.group.CreateGroupRequest;
import com.simul_tech.netgenius.dto.group.GroupReportResponse;
import com.simul_tech.netgenius.dto.group.GroupResponse;
import com.simul_tech.netgenius.dto.group.InvitationResponse;
import com.simul_tech.netgenius.dto.group.InviteRequest;
import com.simul_tech.netgenius.dto.group.StudentResponse;
import com.simul_tech.netgenius.dto.group.UpdateGroupRequest;
import com.simul_tech.netgenius.services.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Управление группами, приглашениями, студентами и отчётами со стороны преподавателя. */
@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
@Tag(name = "Teacher Groups", description = "Группы, приглашения, студенты и отчёты преподавателя")
@SecurityRequirement(name = "bearerAuth")
public class TeacherGroupController {

    private final GroupService groupService;

    @PostMapping("/groups")
    @Operation(summary = "Создать группу")
    @ApiResponse(responseCode = "201", description = "Группа создана")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse group = groupService.createGroup(request.groupName(), request.universityId());
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @GetMapping("/groups")
    @Operation(summary = "Список групп преподавателя")
    public ResponseEntity<List<GroupResponse>> listGroups() {
        return ResponseEntity.ok(groupService.listGroups());
    }

    @GetMapping("/groups/{id}")
    @Operation(summary = "Группа по идентификатору")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroup(id));
    }

    @PutMapping("/groups/{id}")
    @Operation(summary = "Обновить название группы")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateGroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(id, request.groupName()));
    }

    @DeleteMapping("/groups/{id}")
    @Operation(summary = "Удалить группу")
    public ResponseEntity<MessageResponse> deleteGroup(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.deleteGroup(id));
    }

    @PostMapping("/groups/{id}/invite")
    @Operation(summary = "Пригласить студента в группу")
    @ApiResponse(responseCode = "201", description = "Приглашение создано")
    public ResponseEntity<InvitationResponse> invite(@PathVariable Long id,
                                                     @Valid @RequestBody InviteRequest request) {
        InvitationResponse invitation = groupService.invite(id, request.email(), request.studentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }

    @GetMapping("/students")
    @Operation(summary = "Все студенты в группах преподавателя")
    public ResponseEntity<List<StudentResponse>> listStudents() {
        return ResponseEntity.ok(groupService.listStudents());
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Профиль студента (должен быть в группе преподавателя)")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getStudent(id));
    }

    @DeleteMapping("/groups/{groupId}/students/{studentId}")
    @Operation(summary = "Удалить студента из группы")
    public ResponseEntity<MessageResponse> removeStudent(@PathVariable Long groupId,
                                                         @PathVariable Long studentId) {
        return ResponseEntity.ok(groupService.removeStudentFromGroup(groupId, studentId));
    }

    @GetMapping("/groups/{id}/report")
    @Operation(summary = "Отчёт по успеваемости группы")
    public ResponseEntity<GroupReportResponse> report(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.report(id));
    }
}
