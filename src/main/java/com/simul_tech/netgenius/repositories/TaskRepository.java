package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByСтатус(String статус);
    List<Task> findByДатаСозданияBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<Task> findByДатаОкончанияBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<Task> findByIsDone(boolean isDone);
    List<Task> findByСтатусAndIsDone(String статус, boolean isDone);
}
