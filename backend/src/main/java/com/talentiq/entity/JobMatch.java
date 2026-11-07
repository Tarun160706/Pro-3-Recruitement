package com.talentiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_matches")
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Column(name = "skills_score", nullable = false)
    private Integer skillsScore;

    @Column(name = "experience_score", nullable = false)
    private Integer experienceScore;

    @Column(name = "matched_skills", nullable = false, columnDefinition = "TEXT")
    private String matchedSkills; // JSON array

    @Column(name = "missing_skills", nullable = false, columnDefinition = "TEXT")
    private String missingSkills; // JSON array

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "resume_evidence", nullable = false, columnDefinition = "TEXT")
    private String resumeEvidence; // JSON array of quoted lines

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public JobMatch() {
    }

    public JobMatch(Job job, Candidate candidate, Resume resume, Integer overallScore,
                    Integer skillsScore, Integer experienceScore, String matchedSkills,
                    String missingSkills, String explanation, String resumeEvidence) {
        this.job = job;
        this.candidate = candidate;
        this.resume = resume;
        this.overallScore = overallScore;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.explanation = explanation;
        this.resumeEvidence = resumeEvidence;
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

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
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

    public String getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(String matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getResumeEvidence() {
        return resumeEvidence;
    }

    public void setResumeEvidence(String resumeEvidence) {
        this.resumeEvidence = resumeEvidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
