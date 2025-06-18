package com.quiz.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmissionDTO {
    private Map<Long, String> answers; // questionId -> userAnswer
} 