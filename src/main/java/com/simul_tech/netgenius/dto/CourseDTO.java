package com.simul_tech.netgenius.dto;

public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Integer price;
    private Integer duration;
    private Boolean active; // Синхронизировано с сущностью

    // Геттеры/сеттеры
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    // Конструкторы
    public CourseDTO() {}

    public CourseDTO(Long id, String title, String description, Integer price, Integer duration, Boolean is_active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.active = is_active;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Boolean getIs_active() { return active; }
    public void setIs_active(Boolean is_active) { this.active = is_active; }
}