package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.UserLevelScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserLevelScoreRepository extends JpaRepository<UserLevelScore, Long> {
    Optional<UserLevelScore> findByUserIdAndLevel(Long userId, Integer level);
    List<UserLevelScore> findByUserIdIn(Collection<Long> userIds);
}
