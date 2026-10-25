package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.license.AdminLicenseResponse;
import com.simul_tech.netgenius.dto.license.CreateLicenseBatchRequest;
import com.simul_tech.netgenius.dto.license.CreateLicenseRequest;
import com.simul_tech.netgenius.dto.license.CreatedLicenseResponse;
import com.simul_tech.netgenius.dto.license.LicenseBatchResponse;
import com.simul_tech.netgenius.services.LicenseService;
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

/** Администрирование лицензий: выпуск одиночных ключей и партий, просмотр всех лицензий. */
@RestController
@RequestMapping("/admin/licenses")
@RequiredArgsConstructor
@Tag(name = "Admin Licenses", description = "Выпуск и управление лицензиями (только ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminLicenseController {
    private final LicenseService licenseService;

    @PostMapping("/create")
    @Operation(summary = "Создать одиночную лицензию")
    @ApiResponse(responseCode = "201", description = "Лицензия создана")
    public ResponseEntity<CreatedLicenseResponse> create(
            @Valid @RequestBody(required = false) CreateLicenseRequest request) {
        CreateLicenseRequest body = request != null ? request : new CreateLicenseRequest(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.create(body));
    }

    @PostMapping("/batch")
    @Operation(summary = "Создать партию лицензий")
    @ApiResponse(responseCode = "201", description = "Партия лицензий создана")
    public ResponseEntity<LicenseBatchResponse> createBatch(
            @Valid @RequestBody CreateLicenseBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.createBatch(request));
    }

    @GetMapping
    @Operation(summary = "Все лицензии")
    public ResponseEntity<List<AdminLicenseResponse>> findAll() {
        return ResponseEntity.ok(licenseService.findAll());
    }
}
