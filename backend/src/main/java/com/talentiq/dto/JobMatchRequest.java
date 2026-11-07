package com.talentiq.dto;

import jakarta.validation.constraints.NotNull;

public class JobMatchRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    private Long resumeId;

    public JobMatchRequest() {
    }

    public JobMatchRequest(Long jobId, Long candidateId, Long resumeId) {
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.resumeId = resumeId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }
}
