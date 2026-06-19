package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.university.UniversityResponse;
import com.simul_tech.netgenius.services.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Публичный просмотр вузов (GET /universities). */
@RestController
@RequestMapping("/universities")
@RequiredArgsConstructor
@Tag(name = "Universities", description = "Публичный справочник вузов")
public class UniversityController {
    private final UniversityService universityService;

    @GetMapping
    @Operation(summary = "Список вузов (опциональный поиск по названию)")
    public ResponseEntity<List<UniversityResponse>> list(
            @RequestParam(name = "search", required = false) String search) {
        List<UniversityResponse> result = universityService.listUniversities(search).stream()
                .map(UniversityResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Вуз по идентификатору")
    public ResponseEntity<UniversityResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(UniversityResponse.from(universityService.getUniversity(id)));
    }
}
