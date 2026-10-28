package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.verification.AdminVerificationResponse;
import com.simul_tech.netgenius.dto.verification.RejectVerificationRequest;
import com.simul_tech.netgenius.dto.common.ApiError;
import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.services.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Список заявок и их ID (опционально фильтр по статусу)",
            description = "Для одобрения или отклонения используйте поле id из этого списка — это ID заявки. "
                    + "Поле userId содержит ID аккаунта пользователя. Например, для заявки id=4 и userId=26 "
                    + "в approve/reject нужно передать id=4.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список заявок; id — ID заявки, userId — ID пользователя",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AdminVerificationResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Некорректный статус фильтра", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Требуется действительный access-токен", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Требуется роль ADMIN", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<AdminVerificationResponse>> list(
            @RequestParam(value = "status", required = false) VerificationStatus status) {
        return ResponseEntity.ok(verificationService.listForAdmin(status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Одобрить заявку по ID заявки",
            description = "Возьмите id из GET /admin/verifications или номер «Заявка №» из письма. "
                    + "Это ID заявки, а не userId пользователя. Например, заявка № 4 пользователя 26: "
                    + "POST /admin/verifications/4/approve. После одобрения назначается роль STUDENT или TEACHER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заявка одобрена, роль пользователя назначена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminVerificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат ID заявки", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Требуется действительный access-токен", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Требуется роль ADMIN", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Заявка, пользователь или профиль не найдены; причина указана в message. Проверьте, что передан ID заявки из списка, а не userId",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Заявка уже рассмотрена: статус отличается от PENDING", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AdminVerificationResponse> approve(
            @Parameter(description = "ID заявки: поле id из GET /admin/verifications или номер «Заявка №» из письма. ID пользователя (userId) сюда не передаётся", example = "4")
            @PathVariable Long id) {
        return ResponseEntity.ok(verificationService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Отклонить заявку по ID заявки",
            description = "Используйте поле id из GET /admin/verifications или номер «Заявка №» из письма. "
                    + "Например, для заявки id=4 и userId=26 передайте id=4. Комментарий к отказу необязателен; роль пользователя не меняется.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заявка отклонена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminVerificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный ID заявки или тело запроса", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Требуется действительный access-токен", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Требуется роль ADMIN", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Заявка с указанным ID не найдена; нужен ID заявки, а не userId", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Заявка уже рассмотрена: статус отличается от PENDING", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AdminVerificationResponse> reject(
            @Parameter(description = "ID заявки: поле id из GET /admin/verifications или номер «Заявка №» из письма. ID пользователя (userId) сюда не передаётся", example = "4")
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RejectVerificationRequest request) {
        String comment = request != null ? request.comment() : null;
        return ResponseEntity.ok(verificationService.reject(id, comment));
    }
}
