package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.university.CreateUniversityRequest;
import com.simul_tech.netgenius.dto.university.UniversityRequestAdminResponse;
import com.simul_tech.netgenius.dto.university.UniversityResponse;
import com.simul_tech.netgenius.dto.university.UpdateUniversityRequest;
import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.exceptions.BadRequestException;
import com.simul_tech.netgenius.models.University;
import com.simul_tech.netgenius.services.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Администрирование вузов и заявок (база /admin). */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Universities", description = "Администрирование вузов и заявок")
@SecurityRequirement(name = "bearerAuth")
public class AdminUniversityController {
    private final UniversityService universityService;

    @GetMapping("/university-requests")
    @Operation(summary = "Список заявок на создание вуза (опциональный фильтр по статусу)")
    public ResponseEntity<List<UniversityRequestAdminResponse>> listRequests(
            @RequestParam(name = "status", required = false) String status) {
        VerificationStatus filter = parseStatus(status);
        List<UniversityRequestAdminResponse> result = universityService.listRequests(filter).stream()
                .map(UniversityRequestAdminResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/university-requests/{id}/approve")
    @Operation(summary = "Одобрить заявку (создаёт вуз)")
    public ResponseEntity<UniversityResponse> approve(@PathVariable Long id) {
        University university = universityService.approveRequest(id);
        return ResponseEntity.ok(UniversityResponse.from(university));
    }

    @PostMapping("/university-requests/{id}/reject")
    @Operation(summary = "Отклонить заявку")
    public ResponseEntity<UniversityRequestAdminResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(UniversityRequestAdminResponse.from(universityService.rejectRequest(id)));
    }

    @PostMapping("/universities")
    @Operation(summary = "Создать вуз")
    public ResponseEntity<UniversityResponse> create(
            @Valid @RequestBody CreateUniversityRequest request) {
        University university = universityService.createUniversity(
                request.name(), request.faculty(), request.speciality(), request.direction());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UniversityResponse.from(university));
    }

    @PutMapping("/universities/{id}")
    @Operation(summary = "Обновить вуз")
    public ResponseEntity<UniversityResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateUniversityRequest request) {
        University university = universityService.updateUniversity(
                id, request.name(), request.faculty(), request.speciality(), request.direction());
        return ResponseEntity.ok(UniversityResponse.from(university));
    }

    @DeleteMapping("/universities/{id}")
    @Operation(summary = "Удалить вуз")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.noContent().build();
    }

    private VerificationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return VerificationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Недопустимый статус: " + status);
        }
    }
}
