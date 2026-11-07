package com.talentiq.service;

import com.talentiq.dto.ApplicationResponse;
import com.talentiq.dto.CandidateResponse;
import com.talentiq.dto.InterviewSessionResponse;
import com.talentiq.entity.Application;
import com.talentiq.entity.Candidate;
import com.talentiq.entity.InterviewSession;
import com.talentiq.entity.User;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.ApplicationRepository;
import com.talentiq.repository.CandidateRepository;
import com.talentiq.repository.InterviewSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidatePortalService {

    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewSessionRepository sessionRepository;
    private final AuthService authService;
    private final CandidateService candidateService;
    private final InterviewService interviewService;

    public CandidatePortalService(CandidateRepository candidateRepository,
                                  ApplicationRepository applicationRepository,
                                  InterviewSessionRepository sessionRepository,
                                  AuthService authService,
                                  CandidateService candidateService,
                                  InterviewService interviewService) {
        this.candidateRepository = candidateRepository;
        this.applicationRepository = applicationRepository;
        this.sessionRepository = sessionRepository;
        this.authService = authService;
        this.candidateService = candidateService;
        this.interviewService = interviewService;
    }

    @Transactional(readOnly = true)
    public CandidateResponse getMyProfile() {
        User user = authService.getCurrentAuthenticatedUser();
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .or(() -> candidateRepository.findByEmail(user.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("No candidate profile found for user: " + user.getEmail()));

        return candidateService.mapToResponse(candidate);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications() {
        User user = authService.getCurrentAuthenticatedUser();
        List<Application> apps = applicationRepository.findByCandidateUserId(user.getId());
        if (apps.isEmpty()) {
            Candidate candidate = candidateRepository.findByEmail(user.getEmail()).orElse(null);
            if (candidate != null) {
                apps = applicationRepository.findByCandidateId(candidate.getId());
            }
        }
        return apps.stream().map(this::mapApplication).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewSessionResponse> getMyInterviews() {
        User user = authService.getCurrentAuthenticatedUser();
        List<InterviewSession> sessions = sessionRepository.findByCandidateUserId(user.getId());
        if (sessions.isEmpty()) {
            Candidate candidate = candidateRepository.findByEmail(user.getEmail()).orElse(null);
            if (candidate != null) {
                sessions = sessionRepository.findByCandidateId(candidate.getId());
            }
        }
        return sessions.stream().map(interviewService::mapToResponse).collect(Collectors.toList());
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
