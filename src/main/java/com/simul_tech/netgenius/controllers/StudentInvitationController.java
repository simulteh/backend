package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.group.InvitationResponse;
import com.simul_tech.netgenius.services.StudentInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Приглашения в группы со стороны студента: просмотр, принятие, отклонение. */
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "Student Invitations", description = "Приглашения студента в группы")
@SecurityRequirement(name = "bearerAuth")
public class StudentInvitationController {

    private final StudentInvitationService studentInvitationService;

    @GetMapping("/invitations")
    @Operation(summary = "Активные приглашения студента")
    public ResponseEntity<List<InvitationResponse>> listInvitations() {
        return ResponseEntity.ok(studentInvitationService.listInvitations());
    }

    @PostMapping("/invitations/{id}/accept")
    @Operation(summary = "Принять приглашение")
    public ResponseEntity<MessageResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(studentInvitationService.accept(id));
    }

    @PostMapping("/invitations/{id}/decline")
    @Operation(summary = "Отклонить приглашение")
    public ResponseEntity<MessageResponse> decline(@PathVariable Long id) {
        return ResponseEntity.ok(studentInvitationService.decline(id));
    }
}
