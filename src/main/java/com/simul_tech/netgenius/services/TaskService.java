package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.TaskRequestDTO;
import com.simul_tech.netgenius.dto.TaskResponseDTO;
import com.simul_tech.netgenius.models.Task;
import com.simul_tech.netgenius.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponseDTO> getAllTasks(
            String status,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore,
            LocalDateTime dueAfter,
            LocalDateTime dueBefore,
            Boolean isDone) {

        List<Task> tasks = taskRepository.findAll();

        if (status != null && !status.isEmpty()) {
            tasks = tasks.stream()
                         .filter(t -> t.getStatus().equalsIgnoreCase(status))
                         .collect(Collectors.toList());
        }
        if (isDone != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getIsDone() == isDone)
                         .collect(Collectors.toList());
        }
        if (createdAfter != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getCreationDate() != null && !t.getCreationDate().isBefore(createdAfter))
                         .collect(Collectors.toList());
        }
        if (createdBefore != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getCreationDate() != null && !t.getCreationDate().isAfter(createdBefore))
                         .collect(Collectors.toList());
        }
        if (dueAfter != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(dueAfter))
                         .collect(Collectors.toList());
        }
        if (dueBefore != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getDueDate() != null && !t.getDueDate().isAfter(dueBefore))
                         .collect(Collectors.toList());
        }

        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with ID " + id + " not found"));
        return convertToDTO(task);
    }

    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        Task task = new Task();
        task.setName(taskRequestDTO.getName());
        task.setDescription(taskRequestDTO.getDescription());
        task.setStatus(taskRequestDTO.getStatus());
        task.setDueDate(taskRequestDTO.getDueDate());
        task.setIsDone(taskRequestDTO.getIsDone());

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public TaskResponseDTO updateTask(UUID id, TaskRequestDTO taskRequestDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with ID " + id + " not found"));

        task.setName(taskRequestDTO.getName());
        task.setDescription(taskRequestDTO.getDescription());
        task.setStatus(taskRequestDTO.getStatus());
        task.setDueDate(taskRequestDTO.getDueDate());
        task.setIsDone(taskRequestDTO.getIsDone());

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with ID " + id + " not found for deletion");
        }
        taskRepository.deleteById(id);
    }

    private TaskResponseDTO convertToDTO(Task task) {
        return new TaskResponseDTO(
            task.getId(),
            task.getName(),
            task.getDescription(),
            task.getStatus(),
            task.getCreationDate(),
            task.getDueDate(),
            task.getIsDone()
        );
    }
} 