package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.verification.StudentVerificationRequest;
import com.simul_tech.netgenius.dto.verification.TeacherVerificationRequest;
import com.simul_tech.netgenius.dto.verification.VerificationRequestResponse;
import com.simul_tech.netgenius.dto.common.ApiError;
import com.simul_tech.netgenius.services.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Operation(summary = "Подать заявку на роль студента",
            description = "Создаёт PENDING-заявку и отправляет администратору письмо с номером заявки и анкетой. "
                    + "Роль остаётся USER до одобрения администратором. При сбое почты создание заявки откатывается, отправку можно повторить.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заявка создана, письмо отправлено администратору",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VerificationRequestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные поля анкеты", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Требуется действительный access-токен", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Требуется подтверждение email", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Уже есть ожидающая заявка, назначена роль или существует профиль преподавателя", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "Не удалось отправить письмо администратору; заявка откатилась, можно повторить отправку", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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
