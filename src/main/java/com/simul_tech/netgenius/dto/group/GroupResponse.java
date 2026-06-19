package com.simul_tech.netgenius.dto.group;

import com.simul_tech.netgenius.models.Group;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Представление группы. */
@Schema(description = "Группа")
public record GroupResponse(
        Long id,
        String groupName,
        Long universityId,
        Long teacherId,
        Instant createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getGroupName(),
                group.getUniversityId(),
                group.getTeacherId(),
                group.getCreatedAt()
        );
    }
}
