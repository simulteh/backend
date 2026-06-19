package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.sync.SyncProgressRequest;
import com.simul_tech.netgenius.dto.sync.SyncResultResponse;
import com.simul_tech.netgenius.services.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Синхронизация прогресса десктоп-симулятора. Доступ — только роль STUDENT
 * (RBAC по пути /sync/** задан централизованно в SecurityConfig).
 */
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Синхронизация прогресса десктоп-симулятора")
@SecurityRequirement(name = "bearerAuth")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/progress")
    @Operation(summary = "Синхронизировать накопленный прогресс (требуется действующая лицензия)")
    public ResponseEntity<SyncResultResponse> syncProgress(@Valid @RequestBody SyncProgressRequest request) {
        return ResponseEntity.ok(syncService.syncProgress(request));
    }
}
