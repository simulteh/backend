package com.quiz.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizQuestionDTO {
    private Long id;
    private Long quizId;
    private String description;
    private List<String> options;
    private String correctAnswer;
} 