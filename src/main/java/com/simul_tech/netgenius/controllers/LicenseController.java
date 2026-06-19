package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.license.ActivateLicenseRequest;
import com.simul_tech.netgenius.dto.license.ActivatedLicenseResponse;
import com.simul_tech.netgenius.dto.license.LicenseHistoryResponse;
import com.simul_tech.netgenius.dto.license.MyLicenseResponse;
import com.simul_tech.netgenius.services.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Лицензии текущего пользователя: активация, список, история действий. */
@RestController
@RequestMapping("/licenses")
@RequiredArgsConstructor
@Tag(name = "Licenses", description = "Активация и просмотр лицензий пользователя")
@SecurityRequirement(name = "bearerAuth")
public class LicenseController {
    private final LicenseService licenseService;

    @PostMapping("/activate")
    @Operation(summary = "Активировать лицензию по ключу")
    public ResponseEntity<ActivatedLicenseResponse> activate(@Valid @RequestBody ActivateLicenseRequest request) {
        return ResponseEntity.ok(licenseService.activate(request.licenseKey()));
    }

    @GetMapping("/me")
    @Operation(summary = "Лицензии текущего пользователя")
    public ResponseEntity<List<MyLicenseResponse>> myLicenses() {
        return ResponseEntity.ok(licenseService.myLicenses());
    }

    @GetMapping("/history")
    @Operation(summary = "История действий над лицензиями пользователя")
    public ResponseEntity<List<LicenseHistoryResponse>> history() {
        return ResponseEntity.ok(licenseService.history());
    }
}
