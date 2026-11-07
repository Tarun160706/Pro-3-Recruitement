package com.talentiq.dto;

import java.time.LocalDateTime;
import java.util.List;

public class JobMatchResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private Integer overallScore;
    private Integer skillsScore;
    private Integer experienceScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String explanation;
    private List<String> resumeEvidence;
    private LocalDateTime createdAt;

    public JobMatchResponse() {
    }

    public JobMatchResponse(Integer overallScore, Integer skillsScore, Integer experienceScore, List<String> matchedSkills, List<String> missingSkills, String explanation, List<String> resumeEvidence) {
        this.overallScore = overallScore;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.explanation = explanation;
        this.resumeEvidence = resumeEvidence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getSkillsScore() {
        return skillsScore;
    }

    public void setSkillsScore(Integer skillsScore) {
        this.skillsScore = skillsScore;
    }

    public Integer getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(Integer experienceScore) {
        this.experienceScore = experienceScore;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getResumeEvidence() {
        return resumeEvidence;
    }

    public void setResumeEvidence(List<String> resumeEvidence) {
        this.resumeEvidence = resumeEvidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
