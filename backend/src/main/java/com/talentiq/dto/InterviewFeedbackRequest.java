package com.talentiq.dto;

import java.util.List;
import java.util.Map;

public class InterviewFeedbackRequest {
    private String overallFeedback;
    private Integer competencyScore;
    private List<String> strengths;
    private List<String> improvementAreas;
    private List<String> learningRecommendations;
    private Map<Long, Integer> questionRatings;
    private Map<Long, String> candidateAnswers;

    public InterviewFeedbackRequest() {
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

    public Map<Long, Integer> getQuestionRatings() {
        return questionRatings;
    }

    public void setQuestionRatings(Map<Long, Integer> questionRatings) {
        this.questionRatings = questionRatings;
    }

    public Map<Long, String> getCandidateAnswers() {
        return candidateAnswers;
    }

    public void setCandidateAnswers(Map<Long, String> candidateAnswers) {
        this.candidateAnswers = candidateAnswers;
    }
}
