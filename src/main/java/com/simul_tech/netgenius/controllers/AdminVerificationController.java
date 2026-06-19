package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.verification.AdminVerificationResponse;
import com.simul_tech.netgenius.dto.verification.RejectVerificationRequest;
import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.services.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Рассмотрение заявок на подтверждение роли администратором. */
@RestController
@RequestMapping("/admin/verifications")
@RequiredArgsConstructor
@Tag(name = "Admin Verification", description = "Рассмотрение заявок на подтверждение роли")
@SecurityRequirement(name = "bearerAuth")
public class AdminVerificationController {

    private final VerificationService verificationService;

    @GetMapping
    @Operation(summary = "Список заявок (опционально фильтр по статусу)")
    public ResponseEntity<List<AdminVerificationResponse>> list(
            @RequestParam(value = "status", required = false) VerificationStatus status) {
        return ResponseEntity.ok(verificationService.listForAdmin(status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Одобрить заявку")
    public ResponseEntity<AdminVerificationResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Отклонить заявку")
    public ResponseEntity<AdminVerificationResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RejectVerificationRequest request) {
        String comment = request != null ? request.comment() : null;
        return ResponseEntity.ok(verificationService.reject(id, comment));
    }
}
