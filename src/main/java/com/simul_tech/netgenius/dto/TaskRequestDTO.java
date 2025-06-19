package com.simul_tech.netgenius.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class TaskRequestDTO {

    @Schema(description = "Task name", example = "Sample task", required = true)
    @NotBlank(message = "Task name must not be blank")
    private String name;

    @Schema(description = "Task description", example = "Task description", required = false)
    private String description;

    @Schema(description = "Task status", example = "new", required = true)
    @NotBlank(message = "Task status must not be blank")
    private String status;

    @Schema(description = "Task due date (ISO)", example = "2024-06-20T12:00:00", required = false)
    private LocalDateTime dueDate;

    @Schema(description = "Is task done", example = "false", required = true)
    @NotNull(message = "isDone is required")
    private Boolean isDone;

    public TaskRequestDTO() {
    }

    public TaskRequestDTO(String name, String description, String status, LocalDateTime dueDate, boolean isDone) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.isDone = isDone;
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getIsDone() {
        return isDone;
    }

    public void setIsDone(Boolean isDone) {
        this.isDone = isDone;
    }
} 