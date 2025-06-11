package com.simul_tech.netgenius.repositories;

import jakarta.persistence.*;

import java.time.LocalDate;
@Entity
@Table(name= "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private Integer price;
    private Integer duration;

    @Column(name = "is_active")
    private Boolean active; // Изменено на Java-style имя

    private LocalDate created_at;
    private LocalDate updated_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDate.now();
        updated_at = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDate.now();
    }
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    @Override
    public String toString() {
        return "course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", duration=" + duration +
                ", is_active=" + active +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }

    public Course(LocalDate updated_at, LocalDate created_at, Boolean is_active, Integer duration, Integer price, String description, String title, Long id) {
        this.updated_at = updated_at;
        this.created_at = created_at;
        this.active = is_active;
        this.duration = duration;
        this.price = price;
        this.description = description;
        this.title = title;
        this.id = id;
    }

    public Course() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Integer getPrice() {

        return price;
    }

    public void setPrice(Integer price) {

        this.price = price;
    }

    public Integer getDuration() {

        return duration;
    }

    public void setDuration(Integer duration) {

        this.duration = duration;
    }

    public Boolean getIs_active() {

        return active;
    }

    public void setIs_active(Boolean is_active) {

        this.active = is_active;
    }

    public LocalDate getCreated_at() {

        return created_at;
    }

    public void setCreated_at(LocalDate created_at) {
        this.created_at = created_at;
    }

    public LocalDate getUpdated_at() {

        return updated_at;
    }

    public void setUpdated_at(LocalDate updated_at) {
        this.updated_at = updated_at;
    }
}
