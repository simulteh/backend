package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.impls.TokenType;
import com.simul_tech.netgenius.models.MailOutbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MailOutboxRepository extends JpaRepository<MailOutbox, Long> {
    List<MailOutbox> findByUserIdAndTypeAndStatus(Long userId, TokenType type, MailOutbox.Status status);

    @Query("select m.id from MailOutbox m where m.status = 'PENDING' and m.nextAttemptAt <= :now order by m.nextAttemptAt, m.id")
    List<Long> findDueIds(@Param("now") Instant now, Pageable page);

    @Query("select m.userId from MailOutbox m where m.id = :id")
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MailOutbox m where m.id = :id")
    Optional<MailOutbox> findByIdForUpdate(@Param("id") Long id);
}
