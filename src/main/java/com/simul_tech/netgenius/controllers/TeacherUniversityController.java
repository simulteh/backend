package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.university.UniversityRequestCreateRequest;
import com.simul_tech.netgenius.dto.university.UniversityRequestResponse;
import com.simul_tech.netgenius.models.UniversityCreationRequest;
import com.simul_tech.netgenius.services.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Заявки преподавателя на создание вуза (POST /teacher/university-request). */
@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
@Tag(name = "Teacher Universities", description = "Заявки преподавателя на создание вуза")
@SecurityRequirement(name = "bearerAuth")
public class TeacherUniversityController {
    private final UniversityService universityService;

    @PostMapping("/university-request")
    @Operation(summary = "Подать заявку на создание вуза")
    public ResponseEntity<UniversityRequestResponse> createRequest(
            @Valid @RequestBody UniversityRequestCreateRequest request) {
        UniversityCreationRequest created = universityService.createRequest(
                request.universityName(), request.faculty(), request.speciality(), request.direction());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UniversityRequestResponse.from(created));
    }
}
