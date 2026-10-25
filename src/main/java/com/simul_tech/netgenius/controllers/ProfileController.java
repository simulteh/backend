package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.ProfileResponse;
import com.simul_tech.netgenius.dto.common.ApiError;
import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.profile.SaveScoreRequest;
import com.simul_tech.netgenius.dto.profile.TeacherRequestForm;
import com.simul_tech.netgenius.services.CurrentUserService;
import com.simul_tech.netgenius.services.ScoreService;
import com.simul_tech.netgenius.services.TeacherRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Профиль и результаты игры для личных кабинетов.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profile", description = "Профиль, оценки и заявка преподавателя")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final CurrentUserService currentUserService;
    private final ScoreService scoreService;
    private final TeacherRequestService teacherRequestService;

    @GetMapping("/me")
    @Operation(summary = "Профиль текущего пользователя; id используется для добавления студента в группу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя", content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Access-токен отсутствует, некорректен или истёк", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProfileResponse> getInfo() {
        return ResponseEntity.ok(ProfileResponse.from(currentUserService.getCurrentUser()));
    }

    @GetMapping("/scores")
    @Operation(summary = "11 лучших оценок текущего пользователя: обучение и уровни 1–10; null — не пройден")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ровно 11 значений в порядке уровней 0–10", content = @Content(array = @ArraySchema(minItems = 11, maxItems = 11, schema = @Schema(type = "integer", nullable = true, minimum = "0", maximum = "10")))),
            @ApiResponse(responseCode = "401", description = "Требуется access-токен", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<Integer> scores() { return scoreService.current(); }

    @PostMapping("/scores")
    @Operation(summary = "Сохранить лучший результат уровня текущего пользователя; меньший балл не заменяет лучший")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлённые 11 оценок", content = @Content(array = @ArraySchema(minItems = 11, maxItems = 11, schema = @Schema(type = "integer", nullable = true, minimum = "0", maximum = "10")))),
            @ApiResponse(responseCode = "400", description = "level и score должны быть целыми числами 0–10", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Требуется access-токен", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<Integer> saveScore(@Valid @RequestBody SaveScoreRequest request) { return scoreService.save(request); }

    @PostMapping(value = "/teacher-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Отправить заявку преподавателя с выпиской; роль назначается администратором")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заявка сохранена и отправлена по почте"),
            @ApiResponse(responseCode = "400", description = "Не заполнены поля или недопустимый файл", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Требуется access-токен", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Email не подтверждён", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Роль уже назначена или заявка уже на рассмотрении", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "413", description = "Превышен лимит загрузки", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "Не настроен получатель или не удалось отправить письмо", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public MessageResponse teacherRequest(@Valid @ModelAttribute TeacherRequestForm form) {
        teacherRequestService.submit(form);
        return MessageResponse.of("Заявка отправлена на рассмотрение");
    }
}
