package com.talentiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "parsed_skills", columnDefinition = "TEXT")
    private String parsedSkills; // JSON array

    @Column(name = "parsed_experience", columnDefinition = "TEXT")
    private String parsedExperience; // JSON array

    @Column(name = "parsed_education", columnDefinition = "TEXT")
    private String parsedEducation; // JSON array

    @Column(name = "parsed_projects", columnDefinition = "TEXT")
    private String parsedProjects; // JSON array

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths; // JSON array

    @Column(name = "missing_info", columnDefinition = "TEXT")
    private String missingInfo; // JSON array

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Resume() {
    }

    public Resume(Candidate candidate, String fileName, String fileType, Long fileSize, String rawText) {
        this.candidate = candidate;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.rawText = rawText;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getParsedSkills() {
        return parsedSkills;
    }

    public void setParsedSkills(String parsedSkills) {
        this.parsedSkills = parsedSkills;
    }

    public String getParsedExperience() {
        return parsedExperience;
    }

    public void setParsedExperience(String parsedExperience) {
        this.parsedExperience = parsedExperience;
    }

    public String getParsedEducation() {
        return parsedEducation;
    }

    public void setParsedEducation(String parsedEducation) {
        this.parsedEducation = parsedEducation;
    }

    public String getParsedProjects() {
        return parsedProjects;
    }

    public void setParsedProjects(String parsedProjects) {
        this.parsedProjects = parsedProjects;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getMissingInfo() {
        return missingInfo;
    }

    public void setMissingInfo(String missingInfo) {
        this.missingInfo = missingInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
