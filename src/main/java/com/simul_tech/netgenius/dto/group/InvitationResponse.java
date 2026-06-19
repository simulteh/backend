package com.simul_tech.netgenius.dto.group;

import com.simul_tech.netgenius.enums.InvitationStatus;
import com.simul_tech.netgenius.models.GroupInvitation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Представление приглашения в группу. */
@Schema(description = "Приглашение в группу")
public record InvitationResponse(
        Long id,
        Long groupId,
        InvitationStatus status,
        Instant expiresAt
) {
    public static InvitationResponse from(GroupInvitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getGroupId(),
                invitation.getStatus(),
                invitation.getExpiresAt()
        );
    }
}
