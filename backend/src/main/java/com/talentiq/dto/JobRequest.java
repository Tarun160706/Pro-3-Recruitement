package com.talentiq.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class JobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    private String department;
    private String location;
    private String jobType = "FULL_TIME";
    private Integer experienceMin = 0;
    private Integer experienceMax = 10;
    private List<String> requiredSkills;

    @NotBlank(message = "Job description is required")
    private String description;

    private String status = "ACTIVE";

    public JobRequest() {
    }

    public JobRequest(String title, String department, String location, String jobType, Integer experienceMin, Integer experienceMax, List<String> requiredSkills, String description, String status) {
        this.title = title;
        this.department = department;
        this.location = location;
        this.jobType = jobType;
        this.experienceMin = experienceMin;
        this.experienceMax = experienceMax;
        this.requiredSkills = requiredSkills;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Integer getExperienceMin() {
        return experienceMin;
    }

    public void setExperienceMin(Integer experienceMin) {
        this.experienceMin = experienceMin;
    }

    public Integer getExperienceMax() {
        return experienceMax;
    }

    public void setExperienceMax(Integer experienceMax) {
        this.experienceMax = experienceMax;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
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
}
