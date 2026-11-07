package com.talentiq.dto;

import com.talentiq.entity.InterviewQuestion;

public class InterviewQuestionDTO {
    private Long id;
    private String difficulty;
    private String category;
    private String question;
    private String expectedAnswer;
    private String followUp;
    private String candidateAnswer;
    private Integer recruiterRating;
    private Boolean isAiGenerated;

    public InterviewQuestionDTO() {
    }

    public InterviewQuestionDTO(String difficulty, String category, String question, String expectedAnswer, String followUp) {
        this.difficulty = difficulty;
        this.category = category;
        this.question = question;
        this.expectedAnswer = expectedAnswer;
        this.followUp = followUp;
        this.isAiGenerated = true;
    }

    public InterviewQuestionDTO(InterviewQuestion q) {
        this.id = q.getId();
        this.difficulty = q.getDifficulty();
        this.category = q.getCategory();
        this.question = q.getQuestion();
        this.expectedAnswer = q.getExpectedAnswer();
        this.followUp = q.getFollowUp();
        this.candidateAnswer = q.getCandidateAnswer();
        this.recruiterRating = q.getRecruiterRating();
        this.isAiGenerated = q.getIsAiGenerated();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
