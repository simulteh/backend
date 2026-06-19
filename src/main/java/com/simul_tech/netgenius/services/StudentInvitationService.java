package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.group.InvitationResponse;
import com.simul_tech.netgenius.enums.InvitationStatus;
import com.simul_tech.netgenius.exceptions.ConflictException;
import com.simul_tech.netgenius.exceptions.ForbiddenOperationException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.Group;
import com.simul_tech.netgenius.models.GroupInvitation;
import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.models.StudentGroup;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.GroupInvitationRepository;
import com.simul_tech.netgenius.repositories.GroupRepository;
import com.simul_tech.netgenius.repositories.StudentGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Приглашения со стороны студента: просмотр активных приглашений,
 * принятие и отклонение. Просроченные приглашения помечаются EXPIRED.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StudentInvitationService {

    private final GroupInvitationRepository groupInvitationRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final GroupRepository groupRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    /**
     * Активные (PENDING) приглашения студента: по studentId и по email пользователя.
     * Просроченные приглашения помечаются EXPIRED и не возвращаются как активные.
     */
    public List<InvitationResponse> listInvitations() {
        Student student = currentUserService.getCurrentStudent();
        User user = currentUserService.getCurrentUser();
        Instant now = Instant.now();

        // Объединяем по id, чтобы не было дублей (приглашение может совпасть и по studentId, и по email).
        Map<Long, GroupInvitation> pending = new LinkedHashMap<>();
        for (GroupInvitation invitation :
                groupInvitationRepository.findByStudentIdAndStatus(student.getId(), InvitationStatus.PENDING)) {
            pending.put(invitation.getId(), invitation);
        }
        if (user.getEmail() != null) {
            for (GroupInvitation invitation :
                    groupInvitationRepository.findByEmailAndStatus(user.getEmail(), InvitationStatus.PENDING)) {
                pending.put(invitation.getId(), invitation);
            }
        }

        return pending.values().stream()
                .filter(invitation -> {
                    if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(now)) {
                        invitation.setStatus(InvitationStatus.EXPIRED);
                        groupInvitationRepository.save(invitation);
                        return false;
                    }
                    return true;
                })
                .map(InvitationResponse::from)
                .toList();
    }

    public MessageResponse accept(Long invitationId) {
        currentUserService.requireEmailConfirmed();
        Student student = currentUserService.getCurrentStudent();
        GroupInvitation invitation = requireOwnedInvitation(invitationId, student);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ConflictException("Приглашение недоступно для принятия");
        }
        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            groupInvitationRepository.save(invitation);
            throw new ConflictException("приглашение истекло");
        }

        // Инвариант: студент принадлежит только одному вузу — нельзя вступать в группы чужого вуза.
        Group group = groupRepository.findById(invitation.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Группа не найдена: " + invitation.getGroupId()));
        if (student.getUniversityId() != null
                && group.getUniversityId() != null
                && !group.getUniversityId().equals(student.getUniversityId())) {
            throw new ConflictException("Группа принадлежит другому вузу");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        groupInvitationRepository.save(invitation);

        if (!studentGroupRepository.existsByStudentIdAndGroupId(student.getId(), invitation.getGroupId())) {
            StudentGroup link = new StudentGroup();
            link.setStudentId(student.getId());
            link.setGroupId(invitation.getGroupId());
            studentGroupRepository.save(link);
        }

        auditService.log(currentUserService.getCurrentUserId(), "GROUP_INVITATION", invitation.getId(), "ACCEPT");
        return MessageResponse.of("Приглашение принято");
    }

    public MessageResponse decline(Long invitationId) {
        Student student = currentUserService.getCurrentStudent();
        GroupInvitation invitation = requireOwnedInvitation(invitationId, student);

        if (invitation.getStatus() == InvitationStatus.PENDING) {
            invitation.setStatus(InvitationStatus.DECLINED);
            groupInvitationRepository.save(invitation);
        }

        auditService.log(currentUserService.getCurrentUserId(), "GROUP_INVITATION", invitation.getId(), "DECLINE");
        return MessageResponse.of("Приглашение отклонено");
    }

    /**
     * Загружает приглашение и проверяет принадлежность текущему студенту:
     * по studentId приглашения ИЛИ по email текущего пользователя.
     */
    private GroupInvitation requireOwnedInvitation(Long invitationId, Student student) {
        GroupInvitation invitation = groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Приглашение не найдено: " + invitationId));

        boolean byStudentId = invitation.getStudentId() != null
                && invitation.getStudentId().equals(student.getId());
        String userEmail = currentUserService.getCurrentUser().getEmail();
        boolean byEmail = invitation.getEmail() != null
                && userEmail != null
                && invitation.getEmail().equalsIgnoreCase(userEmail);

        if (!byStudentId && !byEmail) {
            throw new ForbiddenOperationException("Приглашение не принадлежит текущему пользователю");
        }
        return invitation;
    }
}
