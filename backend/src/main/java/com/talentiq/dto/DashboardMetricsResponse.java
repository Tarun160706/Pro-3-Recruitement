package com.talentiq.dto;

import java.util.List;
import java.util.Map;

public class DashboardMetricsResponse {
    private long totalCandidates;
    private long activeJobs;
    private long interviewsScheduled;
    private long candidatesAwaitingReview;
    private int hiringRate;
    private List<Map<String, Object>> pipeline;
    private List<ActivityLogDTO> recentActivity;

    public DashboardMetricsResponse() {
    }

    public long getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(long totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public long getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(long activeJobs) {
        this.activeJobs = activeJobs;
    }

    public long getInterviewsScheduled() {
        return interviewsScheduled;
    }

    public void setInterviewsScheduled(long interviewsScheduled) {
        this.interviewsScheduled = interviewsScheduled;
    }

    public long getCandidatesAwaitingReview() {
        return candidatesAwaitingReview;
    }

    public void setCandidatesAwaitingReview(long candidatesAwaitingReview) {
        this.candidatesAwaitingReview = candidatesAwaitingReview;
    }

    public int getHiringRate() {
        return hiringRate;
    }

    public void setHiringRate(int hiringRate) {
        this.hiringRate = hiringRate;
    }

    public List<Map<String, Object>> getPipeline() {
        return pipeline;
    }

    public void setPipeline(List<Map<String, Object>> pipeline) {
        this.pipeline = pipeline;
    }

    public List<ActivityLogDTO> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ActivityLogDTO> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
