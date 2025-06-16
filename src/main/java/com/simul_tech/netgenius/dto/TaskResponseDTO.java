package com.simul_tech.netgenius.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskResponseDTO {

    private UUID id;
    private String название;
    private String описание;
    private String статус;
    private LocalDateTime датаСоздания;
    private LocalDateTime датаОкончания;
    private boolean isDone;

    public TaskResponseDTO() {
    }

    public TaskResponseDTO(UUID id, String название, String описание, String статус, LocalDateTime датаСоздания, LocalDateTime датаОкончания, boolean isDone) {
        this.id = id;
        this.название = название;
        this.описание = описание;
        this.статус = статус;
        this.датаСоздания = датаСоздания;
        this.датаОкончания = датаОкончания;
        this.isDone = isDone;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getDataSozdaniya() {
        return датаСоздания;
    }

    public void setDataSozdaniya(LocalDateTime датаСоздания) {
        this.датаСоздания = датаСоздания;
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