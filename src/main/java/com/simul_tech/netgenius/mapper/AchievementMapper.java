package com.simul_tech.netgenius.mapper;

import com.simul_tech.netgenius.dtos.AchievementDTO;
import com.simul_tech.netgenius.models.Achievement;
import org.springframework.stereotype.Component;

@Component
public class AchievementMapper {
    
    public AchievementDTO toDTO(Achievement achievement) {
        if (achievement == null) {
            return null;
        }
        
        return new AchievementDTO(
            achievement.getId(),
            achievement.getName(),
            achievement.getDescription(),
            achievement.getIconUrl(),
            achievement.getRarity()
        );
    }
    
    public Achievement toEntity(AchievementDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Achievement achievement = new Achievement();
        achievement.setId(dto.getId());
        achievement.setName(dto.getName());
        achievement.setDescription(dto.getDescription());
        achievement.setIconUrl(dto.getIconUrl());
        achievement.setRarity(dto.getRarity());
        
        return achievement;
    }
} 
