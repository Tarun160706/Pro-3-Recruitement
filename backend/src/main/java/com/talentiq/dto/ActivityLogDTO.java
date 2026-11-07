package com.talentiq.dto;

import java.time.LocalDateTime;

public class ActivityLogDTO {
    private Long id;
    private String action;
    private String title;
    private String description;
    private LocalDateTime createdAt;

    public ActivityLogDTO() {
    }

    public ActivityLogDTO(Long id, String action, String title, String description, LocalDateTime createdAt) {
        this.id = id;
        this.action = action;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
