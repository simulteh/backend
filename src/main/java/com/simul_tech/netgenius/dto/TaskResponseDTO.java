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
    private boolean is_done;

    public TaskResponseDTO() {
    }

    public TaskResponseDTO(UUID id, String название, String описание, String статус, LocalDateTime датаСоздания, LocalDateTime датаОкончания, boolean is_done) {
        this.id = id;
        this.название = название;
        this.описание = описание;
        this.статус = статус;
        this.датаСоздания = датаСоздания;
        this.датаОкончания = датаОкончания;
        this.is_done = is_done;
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

    public String getСтатус() {
        return статус;
    }

    public void setСтатус(String статус) {
        this.статус = статус;
    }

    public LocalDateTime getДатаСоздания() {
        return датаСоздания;
    }

    public void setДатаСоздания(LocalDateTime датаСоздания) {
        this.датаСоздания = датаСоздания;
    }

    public LocalDateTime getДатаОкончания() {
        return датаОкончания;
    }

    public void setДатаОкончания(LocalDateTime датаОкончания) {
        this.датаОкончания = датаОкончания;
    }

    public boolean getIs_done() {
        return is_done;
    }

    public void setIs_done(boolean is_done) {
        this.is_done = is_done;
    }
} 