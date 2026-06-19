package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.lab.LabResponse;
import com.simul_tech.netgenius.dto.lab.LeaderboardEntryResponse;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.Lab;
import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.models.StudentLabProgress;
import com.simul_tech.netgenius.repositories.LabRepository;
import com.simul_tech.netgenius.repositories.StudentLabProgressRepository;
import com.simul_tech.netgenius.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Чтение каталога лабораторных работ и формирование лидербордов. */
@Service
@Transactional
@RequiredArgsConstructor
public class LabService {

    /** Максимум строк в лидерборде лабораторной. */
    private static final int LEADERBOARD_LIMIT = 100;

    private final LabRepository labRepository;
    private final StudentLabProgressRepository progressRepository;
    private final StudentRepository studentRepository;

    /** Список активных лабораторных по порядку sortOrder. */
    @Transactional(readOnly = true)
    public List<LabResponse> listActive() {
        return labRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(LabResponse::from)
                .toList();
    }

    /** Одна лабораторная работа (404 если не найдена). */
    @Transactional(readOnly = true)
    public LabResponse getById(Long labId) {
        return LabResponse.from(findLabOrThrow(labId));
    }

    /** Лидерборд лабораторной: топ-100 студентов по лучшей оценке. */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> leaderboard(Long labId) {
        // Проверяем существование лабораторной (404 если нет).
        findLabOrThrow(labId);

        List<StudentLabProgress> ranked =
                progressRepository.findByLabIdAndBestScoreNotNullOrderByBestScoreDesc(labId);

        List<LeaderboardEntryResponse> result = new ArrayList<>();
        int rank = 0;
        for (StudentLabProgress progress : ranked) {
            if (rank >= LEADERBOARD_LIMIT) {
                break;
            }
            rank++;
            Long studentId = progress.getStudentId();
            String studentName = resolveStudentName(studentId);
            result.add(new LeaderboardEntryResponse(rank, studentId, studentName, progress.getBestScore()));
        }
        return result;
    }

    private Lab findLabOrThrow(Long labId) {
        return labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Лабораторная работа не найдена: " + labId));
    }

    private String resolveStudentName(Long studentId) {
        String fullName = studentRepository.findById(studentId)
                .map(Student::getFullName)
                .orElse(null);
        if (fullName == null || fullName.isBlank()) {
            return "Студент #" + studentId;
        }
        return fullName;
    }
}
