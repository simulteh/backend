package com.simul_tech.netgenius.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;

public class TaskRequestDTO {

    @NotBlank(message = "Название не может быть пустым")
    private String название;

    private String описание;

    @NotBlank(message = "Статус не может быть пустым")
    private String статус;

    private LocalDateTime датаОкончания;

    private boolean is_done;

    public TaskRequestDTO() {
    }

    public TaskRequestDTO(String название, String описание, String статус, LocalDateTime датаОкончания, boolean is_done) {
        this.название = название;
        this.описание = описание;
        this.статус = статус;
        this.датаОкончания = датаОкончания;
        this.is_done = is_done;
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