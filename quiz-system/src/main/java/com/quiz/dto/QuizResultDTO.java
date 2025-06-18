package com.quiz.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizResultDTO {
    private int totalQuestions;
    private int correctAnswers;
    private List<Long> wrongQuestionIds;
} 