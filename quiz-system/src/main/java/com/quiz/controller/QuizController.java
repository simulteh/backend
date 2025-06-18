package com.quiz.controller;

import com.quiz.model.Quiz;
import com.quiz.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @PostMapping
    public ResponseEntity<Quiz> addQuiz(@RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizRepository.save(quiz));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
        if (quizRepository.existsById(id)) {
            quiz.setId(id);
            return ResponseEntity.ok(quizRepository.save(quiz));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/active")
    public ResponseEntity<?> findActiveQuizzes() {
        return ResponseEntity.ok(quizRepository.findByActive(true));
    }

    @GetMapping("/time")
    public ResponseEntity<?> findQuizzesByTime(@RequestParam String start, @RequestParam String end) {
        // Здесь нужно добавить логику преобразования строк в LocalDateTime
        return ResponseEntity.ok().build();
    }

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);
    // Пример использования
    public void someMethod() {
        log.info("Это информационное сообщение");
        log.debug("Это отладочное сообщение");
        log.error("Это сообщение об ошибке");
    }
} 
