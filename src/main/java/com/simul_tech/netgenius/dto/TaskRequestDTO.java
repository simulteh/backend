package com.simul_tech.netgenius.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;

public class TaskRequestDTO {

    @NotBlank(message = "Название задачи не может быть пустым")
    private String название;

    private String описание;

    @NotBlank(message = "Статус задачи не может быть пустым")
    private String статус;

    private LocalDateTime датаОкончания;

    private boolean isDone;

    public TaskRequestDTO() {
    }

    public TaskRequestDTO(String название, String описание, String статус, LocalDateTime датаОкончания, boolean isDone) {
        this.название = название;
        this.описание = описание;
        this.статус = статус;
        this.датаОкончания = датаОкончания;
        this.isDone = isDone;
    }

    public String getNazvanie() {
        return название;
    }

    public void setNazvanie(String название) {
        this.название = название;
    }

    public String getOpisanie() {
        return описание;
    }

    public void setOpisanie(String описание) {
        this.описание = описание;
    }

    public String getStatus() {
        return статус;
    }

    public void setStatus(String статус) {
        this.статус = статус;
    }

    public LocalDateTime getDataOkonchaniya() {
        return датаОкончания;
    }

    public void setDataOkonchaniya(LocalDateTime датаОкончания) {
        this.датаОкончания = датаОкончания;
    }

    public boolean isIsDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }
} 