package com.quiz.controller;

import com.quiz.dto.QuizResultDTO;
import com.quiz.dto.QuizSubmissionDTO;
import com.quiz.model.QuizQuestion;
import com.quiz.repository.QuizQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes/{quizId}")
public class QuizPassingController {

    @Autowired
    private QuizQuestionRepository questionRepository;

    @PostMapping("/submit")
    public ResponseEntity<QuizResultDTO> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmissionDTO submission) {
        List<QuizQuestion> questions = questionRepository.findByQuizId(quizId);
        int correct = 0;
        List<Long> wrong = new ArrayList<>();
        for (QuizQuestion q : questions) {
            String userAnswer = submission.getAnswers().get(q.getId());
            if (q.getCorrectAnswer().equalsIgnoreCase(userAnswer != null ? userAnswer.trim() : "")) {
                correct++;
            } else {
                wrong.add(q.getId());
            }
        }
        QuizResultDTO result = new QuizResultDTO();
        result.setTotalQuestions(questions.size());
        result.setCorrectAnswers(correct);
        result.setWrongQuestionIds(wrong);
        return ResponseEntity.ok(result);
    }
} 