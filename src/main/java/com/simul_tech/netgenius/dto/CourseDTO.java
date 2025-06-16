package com.simul_tech.netgenius.dto;

public class CourseDTO {
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

    public CourseDTO( String title, String description, Integer price, Integer duration, Boolean is_active) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.active = is_active;
    }

    // Геттеры и сеттеры

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

}