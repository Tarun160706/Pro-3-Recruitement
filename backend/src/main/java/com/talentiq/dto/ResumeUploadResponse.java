package com.talentiq.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ResumeUploadResponse {
    private Long resumeId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String fileName;
    private Long fileSize;
    private String rawText;
    private ResumeAnalysisResult analysis;
    private LocalDateTime createdAt;

    public ResumeUploadResponse() {
    }

    public List<String> getParsedSkills() {
        return (analysis != null && analysis.getSkills() != null) ? analysis.getSkills() : Collections.emptyList();
    }

    public Integer getRawTextLength() {
        return rawText != null ? rawText.length() : 0;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public ResumeAnalysisResult getAnalysis() {
        return analysis;
    }

    public void setAnalysis(ResumeAnalysisResult analysis) {
        this.analysis = analysis;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
