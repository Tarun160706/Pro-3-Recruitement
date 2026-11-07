package com.talentiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.CandidateRequest;
import com.talentiq.dto.CandidateResponse;
import com.talentiq.entity.Candidate;
import com.talentiq.entity.Resume;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.CandidateRepository;
import com.talentiq.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public CandidateService(CandidateRepository candidateRepository,
                            ResumeRepository resumeRepository,
                            ActivityLogService activityLogService,
                            ObjectMapper objectMapper) {
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CandidateResponse createCandidate(CandidateRequest request) {
        Candidate candidate = new Candidate(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getCurrentTitle(),
                request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0.0,
                request.getLocation(),
                request.getSummary()
        );

        Candidate saved = candidateRepository.save(candidate);
        activityLogService.log("CANDIDATE_CREATED", "Candidate Profile Created",
                "Created candidate profile for " + saved.getFullName());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
        return mapToResponse(candidate);
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> searchCandidates(String search, Double minExp) {
        List<Candidate> candidates;
        if (search == null && minExp == null) {
            candidates = candidateRepository.findAll();
        } else {
            candidates = candidateRepository.searchCandidates(search, minExp);
        }
        return candidates.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public CandidateResponse updateCandidate(Long id, CandidateRequest request) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));

        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setCurrentTitle(request.getCurrentTitle());
        candidate.setYearsOfExperience(request.getYearsOfExperience());
        candidate.setLocation(request.getLocation());
        candidate.setSummary(request.getSummary());

        Candidate updated = candidateRepository.save(candidate);
        return mapToResponse(updated);
    }

    public CandidateResponse mapToResponse(Candidate candidate) {
        CandidateResponse resp = new CandidateResponse();
        resp.setId(candidate.getId());
        if (candidate.getUser() != null) {
            resp.setUserId(candidate.getUser().getId());
        }
        resp.setFullName(candidate.getFullName());
        resp.setEmail(candidate.getEmail());
        resp.setPhone(candidate.getPhone());
        resp.setCurrentTitle(candidate.getCurrentTitle());
        resp.setYearsOfExperience(candidate.getYearsOfExperience());
        resp.setLocation(candidate.getLocation());
        resp.setSummary(candidate.getSummary());
        resp.setCreatedAt(candidate.getCreatedAt());

        Optional<Resume> latestResume = resumeRepository.findFirstByCandidateIdOrderByCreatedAtDesc(candidate.getId());
        if (latestResume.isPresent()) {
            resp.setLatestResumeId(latestResume.get().getId());
            resp.setSkills(parseSkills(latestResume.get().getParsedSkills()));
        } else {
            resp.setSkills(Collections.emptyList());
        }

        return resp;
    }

    private List<String> parseSkills(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
