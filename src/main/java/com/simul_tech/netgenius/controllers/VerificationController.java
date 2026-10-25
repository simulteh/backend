package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.verification.StudentVerificationRequest;
import com.simul_tech.netgenius.dto.verification.TeacherVerificationRequest;
import com.simul_tech.netgenius.dto.verification.VerificationRequestResponse;
import com.simul_tech.netgenius.services.VerificationService;
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

/** Подача заявок на подтверждение роли текущим пользователем. */
@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
@Tag(name = "Verification", description = "Заявки на подтверждение роли студента/преподавателя")
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/student")
    @Operation(summary = "Подать заявку на роль студента")
    @ApiResponse(responseCode = "201", description = "Заявка на роль студента создана")
    public ResponseEntity<VerificationRequestResponse> requestStudent(
            @Valid @RequestBody StudentVerificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.requestStudent(request));
    }

    @PostMapping("/teacher")
    @Operation(summary = "Подать заявку на роль преподавателя")
    @ApiResponse(responseCode = "201", description = "Заявка на роль преподавателя создана")
    public ResponseEntity<VerificationRequestResponse> requestTeacher(
            @Valid @RequestBody TeacherVerificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.requestTeacher(request));
    }

    @GetMapping("/status")
    @Operation(summary = "Заявки текущего пользователя")
    public ResponseEntity<List<VerificationRequestResponse>> status() {
        return ResponseEntity.ok(verificationService.myRequests());
    }
}
