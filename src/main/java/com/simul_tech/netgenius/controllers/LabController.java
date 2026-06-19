package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.lab.LabResponse;
import com.simul_tech.netgenius.dto.lab.LeaderboardEntryResponse;
import com.simul_tech.netgenius.services.LabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Каталог лабораторных работ и лидерборды (для авторизованных пользователей). */
@RestController
@RequestMapping("/labs")
@RequiredArgsConstructor
@Tag(name = "Labs", description = "Каталог лабораторных работ и лидерборды")
@SecurityRequirement(name = "bearerAuth")
public class LabController {

    private final LabService labService;

    @GetMapping
    @Operation(summary = "Список активных лабораторных работ")
    public ResponseEntity<List<LabResponse>> list() {
        return ResponseEntity.ok(labService.listActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Лабораторная работа по идентификатору")
    public ResponseEntity<LabResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(labService.getById(id));
    }

    @GetMapping("/{id}/leaderboard")
    @Operation(summary = "Лидерборд лабораторной (топ-100 по лучшей оценке)")
    public ResponseEntity<List<LeaderboardEntryResponse>> leaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(labService.leaderboard(id));
    }
}
