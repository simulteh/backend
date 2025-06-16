package com.simul_tech.net_genius.controller;

import com.simul_tech.net_genius.dto.UserAchievementDTO;
import com.simul_tech.net_genius.service.UserAchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/achievements")
@RequiredArgsConstructor
@Tag(name = "User Achievement Controller", description = "API для управления достижениями пользователей")
public class UserAchievementController {

    private final UserAchievementService userAchievementService;

    @GetMapping
    @Operation(summary = "Получить все достижения пользователя")
    public ResponseEntity<List<UserAchievementDTO>> getUserAchievements(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String rarity) {
        return ResponseEntity.ok(userAchievementService.getUserAchievements(userId, startDate, endDate, rarity));
    }

    @PostMapping
    @Operation(summary = "Наградить пользователя достижением")
    public ResponseEntity<UserAchievementDTO> awardAchievement(
            @PathVariable Long userId,
            @RequestParam Long achievementId) {
        return ResponseEntity.ok(userAchievementService.awardAchievement(userId, achievementId));
    }

    @DeleteMapping("/{achievementId}")
    @Operation(summary = "Отозвать достижение у пользователя")
    public ResponseEntity<Void> revokeAchievement(
            @PathVariable Long userId,
            @PathVariable Long achievementId) {
        userAchievementService.revokeAchievement(userId, achievementId);
        return ResponseEntity.ok().build();
    }
} 