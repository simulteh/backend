package com.simul_tech.net_genius.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementDTO {
    private Long id;
    private Long userId;
    private AchievementDTO achievement;
    private LocalDateTime awardedAt;
} 