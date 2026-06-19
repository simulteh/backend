package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.enums.InvitationStatus;
import com.simul_tech.netgenius.models.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    Optional<GroupInvitation> findByInviteToken(String inviteToken);
    List<GroupInvitation> findByStudentIdAndStatus(Long studentId, InvitationStatus status);
    List<GroupInvitation> findByStudentId(Long studentId);
    List<GroupInvitation> findByEmailAndStatus(String email, InvitationStatus status);
    List<GroupInvitation> findByGroupId(Long groupId);
    boolean existsByGroupIdAndStudentIdAndStatus(Long groupId, Long studentId, InvitationStatus status);
}
