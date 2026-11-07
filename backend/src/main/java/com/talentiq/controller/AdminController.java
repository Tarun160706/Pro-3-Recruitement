package com.talentiq.controller;

import com.talentiq.dto.RecruiterReviewRequest;
import com.talentiq.dto.UserResponse;
import com.talentiq.entity.VerificationStatus;
import com.talentiq.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/recruiters")
    public ResponseEntity<List<UserResponse>> getRecruiters(
            @RequestParam(required = false) VerificationStatus status) {
        List<UserResponse> recruiters = adminService.getRecruiters(status);
        return ResponseEntity.ok(recruiters);
    }

    @GetMapping("/recruiters/{id}")
    public ResponseEntity<UserResponse> getRecruiterById(@PathVariable Long id) {
        UserResponse recruiter = adminService.getRecruiterById(id);
        return ResponseEntity.ok(recruiter);
    }

    @PostMapping("/recruiters/{id}/approve")
    public ResponseEntity<UserResponse> approveRecruiter(@PathVariable Long id) {
        UserResponse response = adminService.approveRecruiter(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recruiters/{id}/reject")
    public ResponseEntity<UserResponse> rejectRecruiter(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RecruiterReviewRequest request) {
        UserResponse response = adminService.rejectRecruiter(id, request);
        return ResponseEntity.ok(response);
    }
}

