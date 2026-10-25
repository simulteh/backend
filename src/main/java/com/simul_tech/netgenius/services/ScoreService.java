package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.profile.SaveScoreRequest;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.UserLevelScore;
import com.simul_tech.netgenius.repositories.UserLevelScoreRepository;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.util.*;

@Service
@Validated
@RequiredArgsConstructor
public class ScoreService {
    private final UserLevelScoreRepository scores;
    private final UserRepository users;
    private final CurrentUserService currentUserService;

    @Transactional
    public List<Integer> save(@Valid SaveScoreRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        // Защищает и обновление максимума, и первую вставку при одновременных запросах игры.
        users.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        UserLevelScore score = scores.findByUserIdAndLevel(userId, request.level()).orElseGet(() -> {
            UserLevelScore created = new UserLevelScore();
            created.setUserId(userId);
            created.setLevel(request.level());
            return created;
        });
        if (score.getBestScore() == null || request.score() > score.getBestScore()) {
            score.setBestScore(request.score());
            scores.save(score);
        }
        return forUsers(List.of(userId)).get(userId);
    }

    @Transactional(readOnly = true)
    public List<Integer> current() {
        Long userId = currentUserService.getCurrentUserId();
        return forUsers(List.of(userId)).get(userId);
    }

    /** Один запрос для всего состава группы; null и 0 имеют разный смысл. */
    @Transactional(readOnly = true)
    public Map<Long, List<Integer>> forUsers(Collection<Long> userIds) {
        Map<Long, List<Integer>> result = new HashMap<>();
        userIds.forEach(id -> result.put(id, new ArrayList<>(Collections.nCopies(11, null))));
        if (!userIds.isEmpty()) {
            for (UserLevelScore score : scores.findByUserIdIn(userIds)) {
                result.get(score.getUserId()).set(score.getLevel(), score.getBestScore());
            }
        }
        return result;
    }
}
