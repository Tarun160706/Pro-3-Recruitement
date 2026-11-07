package com.talentiq.controller;

import com.talentiq.dto.*;
import com.talentiq.entity.User;
import com.talentiq.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/candidate")
    public ResponseEntity<AuthResponse> registerCandidate(@Valid @RequestBody CandidateRegisterRequest request) {
        AuthResponse response = authService.registerCandidate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register/recruiter")
    public ResponseEntity<AuthResponse> registerRecruiter(@Valid @RequestBody RecruiterRegisterRequest request) {
        AuthResponse response = authService.registerRecruiter(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        UserResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        AuthResponse response = authService.resendVerification(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        AuthResponse authResponse = authService.logout(response);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerLegacy(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
