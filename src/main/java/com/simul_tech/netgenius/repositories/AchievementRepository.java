package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    List<Achievement> findByNameContainingIgnoreCase(String name);
    
    List<Achievement> findByRarity(String rarity);
    
    @Query("SELECT a FROM Achievement a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND a.rarity = :rarity")
    List<Achievement> findByNameContainingAndRarity(@Param("searchTerm") String searchTerm, @Param("rarity") String rarity);
} 