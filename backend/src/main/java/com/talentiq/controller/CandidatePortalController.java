package com.talentiq.controller;

import com.talentiq.dto.ApplicationResponse;
import com.talentiq.dto.CandidateResponse;
import com.talentiq.dto.InterviewSessionResponse;
import com.talentiq.service.CandidatePortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidate")
@PreAuthorize("hasAnyAuthority('ROLE_CANDIDATE', 'ROLE_RECRUITER')")
public class CandidatePortalController {

    private final CandidatePortalService portalService;

    public CandidatePortalController(CandidatePortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/me/profile")
    public ResponseEntity<CandidateResponse> getMyProfile() {
        return ResponseEntity.ok(portalService.getMyProfile());
    }

    @GetMapping("/me/applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(portalService.getMyApplications());
    }

    @GetMapping("/me/interviews")
    public ResponseEntity<List<InterviewSessionResponse>> getMyInterviews() {
        return ResponseEntity.ok(portalService.getMyInterviews());
    }
}
