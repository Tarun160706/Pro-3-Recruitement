package com.talentiq.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InterviewSessionResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long recruiterId;
    private String recruiterName;
    private String title;
    private String status;
    private LocalDateTime scheduledAt;
    private String overallFeedback;
    private Integer competencyScore;
    private List<String> strengths;
    private List<String> improvementAreas;
    private List<String> learningRecommendations;
    private List<InterviewQuestionDTO> questions;
    private LocalDateTime createdAt;

    public InterviewSessionResponse() {
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

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public Integer getCompetencyScore() {
        return competencyScore;
    }

    public void setCompetencyScore(Integer competencyScore) {
        this.competencyScore = competencyScore;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getImprovementAreas() {
        return improvementAreas;
    }

    public void setImprovementAreas(List<String> improvementAreas) {
        this.improvementAreas = improvementAreas;
    }

    public List<String> getLearningRecommendations() {
        return learningRecommendations;
    }

    public void setLearningRecommendations(List<String> learningRecommendations) {
        this.learningRecommendations = learningRecommendations;
    }

    public List<InterviewQuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<InterviewQuestionDTO> questions) {
        this.questions = questions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
