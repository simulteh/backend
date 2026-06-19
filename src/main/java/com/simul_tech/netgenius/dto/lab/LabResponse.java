package com.simul_tech.netgenius.dto.lab;

import com.simul_tech.netgenius.models.Lab;
import io.swagger.v3.oas.annotations.media.Schema;

/** Краткое представление лабораторной работы. */
@Schema(description = "Лабораторная работа")
public record LabResponse(
        Long id,
        String title,
        String description,
        Integer sortOrder
) {
    public static LabResponse from(Lab lab) {
        return new LabResponse(
                lab.getId(),
                lab.getTitle(),
                lab.getDescription(),
                lab.getSortOrder()
        );
    }
}
