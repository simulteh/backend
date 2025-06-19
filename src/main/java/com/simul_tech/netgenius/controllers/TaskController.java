package com.simul_tech.netgenius.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.simul_tech.netgenius.dto.TaskRequestDTO;
import com.simul_tech.netgenius.dto.TaskResponseDTO;
import com.simul_tech.netgenius.services.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Get all tasks", description = "Returns all tasks with optional filtering by status, dates, and completion.")
    @ApiResponse(responseCode = "200", description = "List of tasks successfully retrieved")
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(
            @Parameter(description = "Task status") @RequestParam(required = false) String status,
            @Parameter(description = "Created after (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @Parameter(description = "Created before (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @Parameter(description = "Due after (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueAfter,
            @Parameter(description = "Due before (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueBefore,
            @Parameter(description = "Is task done") @RequestParam(required = false) Boolean isDone) {
        return ResponseEntity.ok(taskService.getAllTasks(status, createdAfter, createdBefore, dueAfter, dueBefore, isDone));
    }

    @Operation(summary = "Get task by ID", description = "Returns a task by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @Parameter(description = "Task UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(summary = "Create new task", description = "Creates a new task with the provided data.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = TaskRequestDTO.class),
                examples = @ExampleObject(value = "{\n  \"name\": \"Sample task\",\n  \"description\": \"Task description\",\n  \"status\": \"new\",\n  \"dueDate\": \"2024-06-20T12:00:00\",\n  \"isDone\": false\n}")
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "Task successfully created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return ResponseEntity.ok(taskService.createTask(taskRequestDTO));
    }

    @Operation(summary = "Update task", description = "Updates an existing task by ID.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = TaskRequestDTO.class),
                examples = @ExampleObject(value = "{\n  \"name\": \"Updated name\",\n  \"description\": \"Updated description\",\n  \"status\": \"in progress\",\n  \"dueDate\": \"2024-07-01T18:00:00\",\n  \"isDone\": true\n}")
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "Task successfully updated")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @Parameter(description = "Task UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskRequestDTO));
    }

    @Operation(summary = "Delete task", description = "Deletes a task by its ID.")
    @ApiResponse(responseCode = "200", description = "Task successfully deleted")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }
} 