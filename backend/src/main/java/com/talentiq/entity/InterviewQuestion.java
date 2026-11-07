package com.talentiq.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(nullable = false, length = 20)
    private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "expected_answer", nullable = false, columnDefinition = "TEXT")
    private String expectedAnswer;

    @Column(name = "follow_up", columnDefinition = "TEXT")
    private String followUp;

    @Column(name = "candidate_answer", columnDefinition = "TEXT")
    private String candidateAnswer;

    @Column(name = "recruiter_rating")
    private Integer recruiterRating;

    @Column(name = "is_ai_generated", nullable = false)
    private Boolean isAiGenerated = true;

    public InterviewQuestion() {
    }

    public InterviewQuestion(String difficulty, String category, String question, String expectedAnswer, String followUp) {
        this.difficulty = difficulty;
        this.category = category;
        this.question = question;
        this.expectedAnswer = expectedAnswer;
        this.followUp = followUp;
        this.isAiGenerated = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewSession getSession() {
        return session;
    }

    public void setSession(InterviewSession session) {
        this.session = session;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    public String getFollowUp() {
        return followUp;
    }

    public void setFollowUp(String followUp) {
        this.followUp = followUp;
    }

    public String getCandidateAnswer() {
        return candidateAnswer;
    }

    public void setCandidateAnswer(String candidateAnswer) {
        this.candidateAnswer = candidateAnswer;
    }

    public Integer getRecruiterRating() {
        return recruiterRating;
    }

    public void setRecruiterRating(Integer recruiterRating) {
        this.recruiterRating = recruiterRating;
    }

    public Boolean getIsAiGenerated() {
        return isAiGenerated;
    }

    public void setIsAiGenerated(Boolean isAiGenerated) {
        this.isAiGenerated = isAiGenerated;
    }
}
