package com.talentiq.dto;

import com.talentiq.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public class RecruiterReviewRequest {

    @NotNull(message = "Review status decision is required (VERIFIED or REJECTED)")
    private VerificationStatus status;

    private String rejectionReason;

    public RecruiterReviewRequest() {
    }

    public RecruiterReviewRequest(VerificationStatus status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}

