package com.simul_tech.netgenius.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public class TaskResponseDTO {

    @Schema(description = "Task UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    @Schema(description = "Task name", example = "Sample task")
    private String name;
    @Schema(description = "Task description", example = "Task description")
    private String description;
    @Schema(description = "Task status", example = "new")
    private String status;
    @Schema(description = "Task creation date (ISO)", example = "2024-06-19T10:00:00")
    private LocalDateTime creationDate;
    @Schema(description = "Task due date (ISO)", example = "2024-06-20T12:00:00")
    private LocalDateTime dueDate;
    @Schema(description = "Is task done", example = "false")
    private boolean isDone;

    public TaskResponseDTO() {
    }

    public TaskResponseDTO(UUID id, String name, String description, String status, LocalDateTime creationDate, LocalDateTime dueDate, boolean isDone) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.creationDate = creationDate;
        this.dueDate = dueDate;
        this.isDone = isDone;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean getIsDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }
} 