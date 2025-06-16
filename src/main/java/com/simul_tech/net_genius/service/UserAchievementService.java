package com.simul_tech.net_genius.service;

import com.simul_tech.net_genius.dto.AchievementDTO;
import com.simul_tech.net_genius.dto.UserAchievementDTO;
import com.simul_tech.net_genius.mapper.AchievementMapper;
import com.simul_tech.net_genius.model.Achievement;
import com.simul_tech.net_genius.model.UserAchievement;
import com.simul_tech.net_genius.repository.AchievementRepository;
import com.simul_tech.net_genius.repository.UserAchievementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementMapper achievementMapper;

    public List<UserAchievementDTO> getUserAchievements(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String rarity) {
        log.debug("Получение достижений пользователя {} с параметрами: startDate={}, endDate={}, rarity={}",
                userId, startDate, endDate, rarity);

        List<UserAchievement> userAchievements;
        if (startDate != null && endDate != null) {
            userAchievements = userAchievementRepository.findByUserIdAndAwardedAtBetween(userId, startDate, endDate);
        } else if (rarity != null) {
            userAchievements = userAchievementRepository.findByUserIdAndAchievementRarity(userId, rarity);
        } else {
            userAchievements = userAchievementRepository.findByUserId(userId);
        }

        return userAchievements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserAchievementDTO awardAchievement(Long userId, Long achievementId) {
        log.debug("Награждение пользователя {} достижением {}", userId, achievementId);

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new EntityNotFoundException("Достижение не найдено с ID: " + achievementId));

        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievement(achievement);
        userAchievement.setAwardedAt(LocalDateTime.now());

        UserAchievement savedUserAchievement = userAchievementRepository.save(userAchievement);
        return convertToDTO(savedUserAchievement);
    }

    public void revokeAchievement(Long userId, Long achievementId) {
        log.debug("Отзыв достижения {} у пользователя {}", achievementId, userId);

        UserAchievement userAchievement = userAchievementRepository.findByUserId(userId).stream()
                .filter(ua -> ua.getAchievement().getId().equals(achievementId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Достижение " + achievementId + " не найдено у пользователя " + userId));

        userAchievementRepository.delete(userAchievement);
    }

    private UserAchievementDTO convertToDTO(UserAchievement userAchievement) {
        AchievementDTO achievementDTO = achievementMapper.toDTO(userAchievement.getAchievement());
        return new UserAchievementDTO(
                userAchievement.getId(),
                userAchievement.getUserId(),
                achievementDTO,
                userAchievement.getAwardedAt()
        );
    }
} 