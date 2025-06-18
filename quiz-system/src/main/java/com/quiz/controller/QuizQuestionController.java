package com.quiz.controller;

import com.quiz.dto.QuizQuestionDTO;
import com.quiz.mapper.QuizQuestionMapper;
import com.quiz.model.QuizQuestion;
import com.quiz.repository.QuizQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
public class QuizQuestionController {

    @Autowired
    private QuizQuestionRepository questionRepository;

    @Autowired
    private QuizQuestionMapper questionMapper;

    @GetMapping
    public ResponseEntity<List<QuizQuestionDTO>> getAllQuestions(@PathVariable Long quizId) {
        List<QuizQuestion> questions = questionRepository.findByQuizId(quizId);
        List<QuizQuestionDTO> dtos = questions.stream()
                .map(questionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizQuestionDTO> getQuestion(@PathVariable Long quizId, @PathVariable Long id) {
        return questionRepository.findByIdAndQuizId(id, quizId)
                .map(questionMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<QuizQuestionDTO> createQuestion(@PathVariable Long quizId, @RequestBody QuizQuestionDTO dto) {
        dto.setQuizId(quizId);
        QuizQuestion question = questionMapper.toEntity(dto);
        QuizQuestion saved = questionRepository.save(question);
        return ResponseEntity.ok(questionMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizQuestionDTO> updateQuestion(
            @PathVariable Long quizId,
            @PathVariable Long id,
            @RequestBody QuizQuestionDTO dto) {
        return questionRepository.findByIdAndQuizId(id, quizId)
                .map(question -> {
                    questionMapper.updateQuestionFromDto(dto, question);
                    QuizQuestion updated = questionRepository.save(question);
                    return ResponseEntity.ok(questionMapper.toDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long quizId, @PathVariable Long id) {
        return questionRepository.findByIdAndQuizId(id, quizId)
                .map(question -> {
                    questionRepository.delete(question);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 