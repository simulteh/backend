package com.simul_tech.netgenius.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Создание учебной группы (POST /teacher/groups). */
@Schema(description = "Запрос на создание группы")
public record CreateGroupRequest(
        @Schema(description = "Название группы", example = "ИВТ-21-1")
        @NotBlank(message = "Название группы обязательно")
        String groupName,

        @Schema(description = "Идентификатор вуза", example = "1")
        @NotNull(message = "Вуз обязателен")
        Long universityId
) {
}
