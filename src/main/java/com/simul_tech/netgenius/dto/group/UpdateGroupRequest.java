package com.simul_tech.netgenius.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Обновление группы (PUT /teacher/groups/{id}). */
@Schema(description = "Запрос на обновление группы")
public record UpdateGroupRequest(
        @Schema(description = "Новое название группы", example = "ИВТ-21-2")
        @NotBlank(message = "Название группы обязательно")
        String groupName
) {
}
