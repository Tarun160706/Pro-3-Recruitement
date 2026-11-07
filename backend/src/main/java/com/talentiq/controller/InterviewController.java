package com.talentiq.controller;

import com.talentiq.dto.GenerateInterviewRequest;
import com.talentiq.dto.InterviewFeedbackRequest;
import com.talentiq.dto.InterviewQuestionDTO;
import com.talentiq.dto.InterviewSessionResponse;
import com.talentiq.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<InterviewSessionResponse> generateInterview(@Valid @RequestBody GenerateInterviewRequest request) {
        InterviewSessionResponse response = interviewService.generateInterview(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewSessionResponse> getInterviewById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getInterviewById(id));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<List<InterviewSessionResponse>> getInterviewsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(interviewService.getInterviewsByJob(jobId));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<InterviewSessionResponse>> getInterviewsByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(interviewService.getInterviewsByCandidate(candidateId));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<InterviewQuestionDTO> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody InterviewQuestionDTO updateDto) {
        return ResponseEntity.ok(interviewService.updateQuestion(questionId, updateDto));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<InterviewSessionResponse> submitFeedback(
            @PathVariable Long id,
            @RequestBody InterviewFeedbackRequest request) {
        return ResponseEntity.ok(interviewService.submitFeedback(id, request));
    }
}
