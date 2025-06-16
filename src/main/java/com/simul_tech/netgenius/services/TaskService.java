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
                         .filter(t -> t.isIsDone() == isDone)
                         .collect(Collectors.toList());
        }
        if (createdAfter != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getDataSozdaniya() != null && !t.getDataSozdaniya().isBefore(createdAfter))
                         .collect(Collectors.toList());
        }
        if (createdBefore != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getDataSozdaniya() != null && !t.getDataSozdaniya().isAfter(createdBefore))
                         .collect(Collectors.toList());
        }
        if (dueAfter != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getDataOkonchaniya() != null && !t.getDataOkonchaniya().isBefore(dueAfter))
                         .collect(Collectors.toList());
        }
        if (dueBefore != null) {
            tasks = tasks.stream()
                         .filter(t -> t.getDataOkonchaniya() != null && !t.getDataOkonchaniya().isAfter(dueBefore))
                         .collect(Collectors.toList());
        }

        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задание с ID " + id + " не найдено"));
        return convertToDTO(task);
    }

    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        Task task = new Task();
        task.setNazvanie(taskRequestDTO.getNazvanie());
        task.setOpisanie(taskRequestDTO.getOpisanie());
        task.setStatus(taskRequestDTO.getStatus());
        task.setDataOkonchaniya(taskRequestDTO.getDataOkonchaniya());
        task.setIsDone(taskRequestDTO.isIsDone());

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public TaskResponseDTO updateTask(UUID id, TaskRequestDTO taskRequestDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задание с ID " + id + " не найдено"));

        task.setNazvanie(taskRequestDTO.getNazvanie());
        task.setOpisanie(taskRequestDTO.getOpisanie());
        task.setStatus(taskRequestDTO.getStatus());
        task.setDataOkonchaniya(taskRequestDTO.getDataOkonchaniya());
        task.setIsDone(taskRequestDTO.isIsDone());

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задание с ID " + id + " не найдено для удаления");
        }
        taskRepository.deleteById(id);
    }

    private TaskResponseDTO convertToDTO(Task task) {
        return new TaskResponseDTO(
            task.getId(),
            task.getNazvanie(),
            task.getOpisanie(),
            task.getStatus(),
            task.getDataSozdaniya(),
            task.getDataOkonchaniya(),
            task.isIsDone()
        );
    }
} 