package com.simul_tech.netgenius.controller;

import com.simul_tech.netgenius.dto.AchievementDTO;
import com.simul_tech.netgenius.model.Achievement;
import com.simul_tech.netgenius.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievement Controller", description = "API для управления достижениями")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(summary = "Получить список всех достижений")
    public ResponseEntity<List<AchievementDTO>> getAllAchievements(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String rarity) {
        return ResponseEntity.ok(achievementService.getAllAchievements(name, rarity));
    }

    @PostMapping
    @Operation(summary = "Создать новое достижение")
    public ResponseEntity<AchievementDTO> createAchievement(@RequestBody AchievementDTO achievementDTO) {
        return ResponseEntity.ok(achievementService.createAchievement(achievementDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить достижение по ID")
    public ResponseEntity<AchievementDTO> getAchievementById(@PathVariable Long id) {
        return ResponseEntity.ok(achievementService.getAchievementById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить достижение")
    public ResponseEntity<AchievementDTO> updateAchievement(
            @PathVariable Long id,
            @RequestBody AchievementDTO achievementDTO) {
        return ResponseEntity.ok(achievementService.updateAchievement(id, achievementDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить достижение")
    public ResponseEntity<Void> deleteAchievement(@PathVariable Long id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.ok().build();
    }
} 
