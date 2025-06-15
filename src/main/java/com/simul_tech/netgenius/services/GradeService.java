package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.entity.Grade;
import com.simul_tech.netgenius.models.GradeDTO;
import com.simul_tech.netgenius.repositories.GradeRepository;
import com.simul_tech.netgenius.mappers.GradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok аннотация для автоматической инъекции через конструктор
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;

    @Transactional(readOnly = true)
    public List<GradeDTO> getAllGrades() {
        return gradeRepository.findAll()
                .stream()
                .map(gradeMapper::toDto) // Используем маппер для преобразования сущности в DTO
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<GradeDTO> getGradeById(Long id) {
        return gradeRepository.findById(id)
                .map(gradeMapper::toDto); // Преобразуем сущность в DTO, если найдена
    }

    @Transactional
    public GradeDTO createGrade(GradeDTO gradeDTO) {
        Grade grade = gradeMapper.toEntity(gradeDTO); // Преобразуем DTO в сущность
        Grade savedGrade = gradeRepository.save(grade);
        return gradeMapper.toDto(savedGrade); // Возвращаем сохраненную сущность как DTO
    }

    @Transactional
    public Optional<GradeDTO> updateGrade(Long id, GradeDTO updatedGradeDTO) {
        return gradeRepository.findById(id).map(existingGrade -> {
            // Обновляем поля существующей сущности из DTO.
            // Можно использовать updateGradeFromDto из маппера, если он реализован.
            existingGrade.setScore(updatedGradeDTO.getScore());
            existingGrade.setFeedback(updatedGradeDTO.getFeedback());
            existingGrade.setSubmissionDate(updatedGradeDTO.getSubmissionDate());
            // Если есть другие поля, добавь их здесь

            Grade savedGrade = gradeRepository.save(existingGrade);
            return gradeMapper.toDto(savedGrade);
        });
    }

    @Transactional
    public boolean deleteGrade(Long id) {
        if (gradeRepository.existsById(id)) {
            gradeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByScore(Integer score) {
        List<Grade> grades = gradeRepository.findByScore(score);
        return grades.stream()
                .map(gradeMapper::toDto)
                .collect(Collectors.toList());
    }

}