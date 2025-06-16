package com.simul_tech.net_genius.service;

import com.simul_tech.net_genius.dto.AchievementDTO;
import com.simul_tech.net_genius.mapper.AchievementMapper;
import com.simul_tech.net_genius.model.Achievement;
import com.simul_tech.net_genius.repository.AchievementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementMapper achievementMapper;

    public List<AchievementDTO> getAllAchievements(String name, String rarity) {
        log.debug("Получение списка достижений с параметрами: name={}, rarity={}", name, rarity);
        
        List<Achievement> achievements;
        if (name != null && rarity != null) {
            achievements = achievementRepository.findByNameContainingAndRarity(name, rarity);
        } else if (name != null) {
            achievements = achievementRepository.findByNameContainingIgnoreCase(name);
        } else if (rarity != null) {
            achievements = achievementRepository.findByRarity(rarity);
        } else {
            achievements = achievementRepository.findAll();
        }
        
        return achievements.stream()
                .map(achievementMapper::toDTO)
                .collect(Collectors.toList());
    }

    public AchievementDTO createAchievement(AchievementDTO achievementDTO) {
        log.debug("Создание нового достижения: {}", achievementDTO);
        Achievement achievement = achievementMapper.toEntity(achievementDTO);
        Achievement savedAchievement = achievementRepository.save(achievement);
        return achievementMapper.toDTO(savedAchievement);
    }

    public AchievementDTO getAchievementById(Long id) {
        log.debug("Получение достижения по ID: {}", id);
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Достижение не найдено с ID: " + id));
        return achievementMapper.toDTO(achievement);
    }

    public AchievementDTO updateAchievement(Long id, AchievementDTO achievementDTO) {
        log.debug("Обновление достижения с ID {}: {}", id, achievementDTO);
        if (!achievementRepository.existsById(id)) {
            throw new EntityNotFoundException("Достижение не найдено с ID: " + id);
        }
        
        Achievement achievement = achievementMapper.toEntity(achievementDTO);
        achievement.setId(id);
        Achievement updatedAchievement = achievementRepository.save(achievement);
        return achievementMapper.toDTO(updatedAchievement);
    }

    public void deleteAchievement(Long id) {
        log.debug("Удаление достижения с ID: {}", id);
        if (!achievementRepository.existsById(id)) {
            throw new EntityNotFoundException("Достижение не найдено с ID: " + id);
        }
        achievementRepository.deleteById(id);
    }
} 