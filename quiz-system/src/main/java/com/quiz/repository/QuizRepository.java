package com.quiz.repository;

import com.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByActive(boolean active);
    List<Quiz> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Quiz> findByGroupId(String groupId);
} 