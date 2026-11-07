package com.talentiq.controller;

import com.talentiq.dto.ApplicationRequest;
import com.talentiq.dto.ApplicationResponse;
import com.talentiq.dto.CandidateRequest;
import com.talentiq.dto.CandidateResponse;
import com.talentiq.entity.Application;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.ApplicationRepository;
import com.talentiq.service.ActivityLogService;
import com.talentiq.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidates")
@PreAuthorize("hasAuthority('ROLE_RECRUITER')")
public class CandidateController {

    private final CandidateService candidateService;
    private final ApplicationRepository applicationRepository;
    private final ActivityLogService activityLogService;

    public CandidateController(CandidateService candidateService,
                               ApplicationRepository applicationRepository,
                               ActivityLogService activityLogService) {
        this.candidateService = candidateService;
        this.applicationRepository = applicationRepository;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAllCandidates(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minExp) {
        return ResponseEntity.ok(candidateService.searchCandidates(search, minExp));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> createCandidate(@Valid @RequestBody CandidateRequest request) {
        CandidateResponse response = candidateService.createCandidate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.ok(candidateService.updateCandidate(id, request));
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplications(@PathVariable Long id) {
        List<ApplicationResponse> apps = applicationRepository.findByCandidateId(id).stream()
                .map(this::mapApplication)
                .collect(Collectors.toList());
        return ResponseEntity.ok(apps);
    }

    @PatchMapping("/{id}/applications/{appId}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @PathVariable Long appId,
            @RequestBody ApplicationRequest request) {
        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + appId));

        if (request.getStatus() != null) {
            application.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getStage() != null) {
            application.setStage(request.getStage());
        }
        if (request.getRecruiterNotes() != null) {
            application.setRecruiterNotes(request.getRecruiterNotes());
        }

        Application saved = applicationRepository.save(application);
        activityLogService.log("STATUS_UPDATED", "Candidate Pipeline Status Updated",
                "Updated " + saved.getCandidate().getFullName() + " status to " + saved.getStatus() + " (" + saved.getStage() + ").");

        return ResponseEntity.ok(mapApplication(saved));
    }

    private ApplicationResponse mapApplication(Application app) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(app.getId());
        resp.setJobId(app.getJob().getId());
        resp.setJobTitle(app.getJob().getTitle());
        resp.setDepartment(app.getJob().getDepartment());
        resp.setCandidateId(app.getCandidate().getId());
        resp.setCandidateName(app.getCandidate().getFullName());
        resp.setCandidateEmail(app.getCandidate().getEmail());
        resp.setCandidateTitle(app.getCandidate().getCurrentTitle());
        if (app.getResume() != null) {
            resp.setResumeId(app.getResume().getId());
        }
        resp.setStatus(app.getStatus());
        resp.setStage(app.getStage());
        resp.setRecruiterNotes(app.getRecruiterNotes());
        resp.setCreatedAt(app.getCreatedAt());
        resp.setUpdatedAt(app.getUpdatedAt());
        return resp;
    }
}
