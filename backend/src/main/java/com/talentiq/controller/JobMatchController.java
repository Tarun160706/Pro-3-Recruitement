package com.talentiq.controller;

import com.talentiq.dto.JobMatchRequest;
import com.talentiq.dto.JobMatchResponse;
import com.talentiq.service.JobMatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@PreAuthorize("hasAuthority('ROLE_RECRUITER')")
public class JobMatchController {

    private final JobMatchService jobMatchService;

    public JobMatchController(JobMatchService jobMatchService) {
        this.jobMatchService = jobMatchService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<JobMatchResponse> evaluateMatch(@Valid @RequestBody JobMatchRequest request) {
        JobMatchResponse response = jobMatchService.evaluateMatch(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobMatchResponse> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(jobMatchService.getMatchById(id));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobMatchResponse>> getMatchesByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobMatchService.getMatchesByJob(jobId));
    }
}
