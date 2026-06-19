package com.simul_tech.netgenius.dto.license;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Результат создания партии лицензий (POST /admin/licenses/batch). */
@Schema(description = "Созданная партия лицензий")
public record LicenseBatchResponse(
        Long batchId,
        int quantity,
        List<String> keys
) {
}
