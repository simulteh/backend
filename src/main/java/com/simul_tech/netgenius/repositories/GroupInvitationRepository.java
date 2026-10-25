package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.enums.InvitationStatus;
import com.simul_tech.netgenius.models.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    List<GroupInvitation> findByStudentIdAndStatus(Long studentId, InvitationStatus status);
    List<GroupInvitation> findByEmailAndStatus(String email, InvitationStatus status);
    boolean existsByGroupIdAndStudentIdAndStatus(Long groupId, Long studentId, InvitationStatus status);
}
