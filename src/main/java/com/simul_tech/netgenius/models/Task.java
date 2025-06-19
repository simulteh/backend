package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(columnDefinition = "TEXT", name = "description")
    private String description;

    @Column(nullable = false, name = "status")
    private String status;

    @Column(nullable = false, name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(nullable = false, name = "is_done")
    private boolean isDone;

    public Task() {
    }

    public Task(String name, String description, String status, LocalDateTime creationDate, LocalDateTime dueDate, boolean isDone) {
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

    @PrePersist
    protected void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }
    }
}
