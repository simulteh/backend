package com.simul_tech.net_genius.repository;

import com.simul_tech.net_genius.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    List<UserAchievement> findByUserId(Long userId);
    
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.awardedAt BETWEEN :startDate AND :endDate")
    List<UserAchievement> findByUserIdAndAwardedAtBetween(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.achievement.rarity = :rarity")
    List<UserAchievement> findByUserIdAndAchievementRarity(
        @Param("userId") Long userId,
        @Param("rarity") String rarity
    );
} 