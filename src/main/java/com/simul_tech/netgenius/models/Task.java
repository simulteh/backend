package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String название;

    @Column(columnDefinition = "TEXT")
    private String описание;

    @Column(nullable = false)
    private String статус;

    @Column(nullable = false)
    private LocalDateTime датаСоздания;

    private LocalDateTime датаОкончания;

    @Column(nullable = false)
    private boolean isDone;

    public Task() {
    }

    public Task(String название, String описание, String статус, LocalDateTime датаСоздания, LocalDateTime датаОкончания, boolean isDone) {
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

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (датаСоздания == null) {
            датаСоздания = LocalDateTime.now();
        }
    }
}
