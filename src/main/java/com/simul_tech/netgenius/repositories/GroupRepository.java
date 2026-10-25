package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Group;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByTeacherId(Long teacherId);
    Optional<Group> findByIdAndTeacherId(Long id, Long teacherId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Group g where g.id = :id")
    Optional<Group> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Group g where g.inviteCode = :code")
    Optional<Group> findByInviteCodeForUpdate(@Param("code") String code);
}
