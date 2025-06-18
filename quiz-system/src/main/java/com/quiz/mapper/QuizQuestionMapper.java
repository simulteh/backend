package com.quiz.mapper;

import com.quiz.dto.QuizQuestionDTO;
import com.quiz.model.QuizQuestion;
import org.springframework.stereotype.Component;

@Component
public class QuizQuestionMapper {
    
    public QuizQuestionDTO toDto(QuizQuestion question) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(question.getId());
        dto.setQuizId(question.getQuizId());
        dto.setDescription(question.getDescription());
        dto.setOptions(question.getOptions());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        return dto;
    }

    public QuizQuestion toEntity(QuizQuestionDTO dto) {
        QuizQuestion question = new QuizQuestion();
        question.setId(dto.getId());
        question.setQuizId(dto.getQuizId());
        question.setDescription(dto.getDescription());
        question.setOptions(dto.getOptions());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        return question;
    }

    public void updateQuestionFromDto(QuizQuestionDTO dto, QuizQuestion question) {
        question.setDescription(dto.getDescription());
        question.setOptions(dto.getOptions());
        question.setCorrectAnswer(dto.getCorrectAnswer());
    }
} 