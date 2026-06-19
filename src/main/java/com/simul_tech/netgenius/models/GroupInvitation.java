package com.simul_tech.netgenius.models;

import com.simul_tech.netgenius.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Приглашение студента в группу. Действует 7 дней. */
@Entity
@Getter
@Setter
@Table(name = "group_invitations",
        uniqueConstraints = {@UniqueConstraint(name = "uk_invite_token", columnNames = "invite_token")},
        indexes = {@Index(name = "idx_invitations_student", columnList = "student_id")})
public class GroupInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    /** Может быть null, если приглашают по email пользователя, ещё не ставшего студентом. */
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "email")
    private String email;

    @Column(name = "invite_token", nullable = false, unique = true)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = InvitationStatus.PENDING;
        }
    }
}
