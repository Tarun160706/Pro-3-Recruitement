package com.talentiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.ResumeAnalysisResult;
import com.talentiq.dto.ResumeUploadResponse;
import com.talentiq.entity.Application;
import com.talentiq.entity.Candidate;
import com.talentiq.entity.Job;
import com.talentiq.entity.Resume;
import com.talentiq.entity.Role;
import com.talentiq.entity.User;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.ApplicationRepository;
import com.talentiq.repository.CandidateRepository;
import com.talentiq.repository.JobRepository;
import com.talentiq.repository.ResumeRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    private final ResumeParserService resumeParserService;
    private final GeminiAiService geminiAiService;
    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ActivityLogService activityLogService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public ResumeService(ResumeParserService resumeParserService,
                         GeminiAiService geminiAiService,
                         ResumeRepository resumeRepository,
                         CandidateRepository candidateRepository,
                         JobRepository jobRepository,
                         ApplicationRepository applicationRepository,
                         ActivityLogService activityLogService,
                         AuthService authService,
                         ObjectMapper objectMapper) {
        this.resumeParserService = resumeParserService;
        this.geminiAiService = geminiAiService;
        this.resumeRepository = resumeRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.activityLogService = activityLogService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeUploadResponse uploadAndAnalyze(MultipartFile file, Long candidateId, Long jobId) {
        // Step 1: Extract text via PDFBox
        String rawText = resumeParserService.extractText(file);

        // Step 2: AI analysis via Gemini / NLP Engine
        ResumeAnalysisResult analysis = geminiAiService.analyzeResume(rawText);

        // Step 3: Resolve or create Candidate
        Optional<User> authUserOpt = authService.getOptionalAuthenticatedUser();
        Candidate candidate;
        if (candidateId != null) {
            candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));
            if (authUserOpt.isPresent() && authUserOpt.get().getRole() == Role.ROLE_CANDIDATE) {
                User candUser = authUserOpt.get();
                boolean isOwner = (candidate.getUser() != null && candidate.getUser().getId().equals(candUser.getId()))
                        || candidate.getEmail().equalsIgnoreCase(candUser.getEmail());
                if (!isOwner) {
                    throw new AccessDeniedException("Access denied: You cannot upload a resume for another candidate");
                }
            }
            // Update candidate details if missing
            if (candidate.getCurrentTitle() == null && analysis.getCurrentTitle() != null) {
                candidate.setCurrentTitle(analysis.getCurrentTitle());
            }
            if (candidate.getYearsOfExperience() == null || candidate.getYearsOfExperience() == 0.0) {
                candidate.setYearsOfExperience(analysis.getYearsOfExperience());
            }
            if (candidate.getSummary() == null && analysis.getSummary() != null) {
                candidate.setSummary(analysis.getSummary());
            }
            candidateRepository.save(candidate);
        } else {
            if (authUserOpt.isPresent() && authUserOpt.get().getRole() == Role.ROLE_CANDIDATE) {
                User candUser = authUserOpt.get();
                candidate = candidateRepository.findByUserId(candUser.getId())
                        .or(() -> candidateRepository.findByEmail(candUser.getEmail()))
                        .orElse(null);
                if (candidate == null) {
                    candidate = new Candidate(
                            candUser.getFullName(),
                            candUser.getEmail(),
                            analysis.getPhone() != null ? analysis.getPhone() : candUser.getPhone(),
                            analysis.getCurrentTitle(),
                            analysis.getYearsOfExperience(),
                            analysis.getLocation(),
                            analysis.getSummary()
                    );
                    candidate.setUser(candUser);
                    candidate = candidateRepository.save(candidate);
                }
            } else {
                String name = analysis.getCandidateName() != null ? analysis.getCandidateName() : "Unknown Candidate";
                String email = analysis.getEmail() != null ? analysis.getEmail() : ("candidate_" + System.currentTimeMillis() + "@talentiq.local");
                Optional<Candidate> existing = candidateRepository.findByEmail(email);
                if (existing.isPresent()) {
                    candidate = existing.get();
                } else {
                    candidate = new Candidate(
                            name,
                            email,
                            analysis.getPhone(),
                            analysis.getCurrentTitle(),
                            analysis.getYearsOfExperience(),
                            analysis.getLocation(),
                            analysis.getSummary()
                    );
                    candidate = candidateRepository.save(candidate);
                }
            }
        }

        // Step 4: Persist Resume
        Resume resume = new Resume(
                candidate,
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf",
                file.getContentType() != null ? file.getContentType() : "application/pdf",
                file.getSize(),
                rawText
        );

        try {
            resume.setParsedSkills(objectMapper.writeValueAsString(analysis.getSkills()));
            resume.setParsedExperience(objectMapper.writeValueAsString(analysis.getExperience()));
            resume.setParsedEducation(objectMapper.writeValueAsString(analysis.getEducation()));
            resume.setParsedProjects(objectMapper.writeValueAsString(analysis.getProjects()));
            resume.setStrengths(objectMapper.writeValueAsString(analysis.getStrengths()));
            resume.setMissingInfo(objectMapper.writeValueAsString(analysis.getMissingInfo()));
        } catch (Exception ignored) {
        }

        Resume savedResume = resumeRepository.save(resume);

        // Step 5: If jobId provided, create or link Application
        if (jobId != null) {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                Optional<Application> existingApp = applicationRepository.findByJobIdAndCandidateId(jobId, candidate.getId());
                if (existingApp.isEmpty()) {
                    Application app = new Application(job, candidate, savedResume, "SCREENED", "Resume Screening", "Resume parsed and profile analyzed via AI.");
                    applicationRepository.save(app);
                }
            }
        }

        activityLogService.log("RESUME_ANALYZED", "Resume Analyzed by AI",
                "Successfully analyzed resume for " + candidate.getFullName() + " (" + (analysis.getSkills() != null ? analysis.getSkills().size() : 0) + " skills extracted).");

        ResumeUploadResponse response = new ResumeUploadResponse();
        response.setResumeId(savedResume.getId());
        response.setCandidateId(candidate.getId());
        response.setCandidateName(candidate.getFullName());
        response.setCandidateEmail(candidate.getEmail());
        response.setFileName(savedResume.getFileName());
        response.setFileSize(savedResume.getFileSize());
        response.setRawText(rawText);
        response.setAnalysis(analysis);
        response.setCreatedAt(savedResume.getCreatedAt());

        return response;
    }

    @Transactional(readOnly = true)
    public ResumeUploadResponse getResumeById(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        Optional<User> authUserOpt = authService.getOptionalAuthenticatedUser();
        if (authUserOpt.isPresent() && authUserOpt.get().getRole() == Role.ROLE_CANDIDATE) {
            User candUser = authUserOpt.get();
            boolean isOwner = (resume.getCandidate().getUser() != null && resume.getCandidate().getUser().getId().equals(candUser.getId()))
                    || resume.getCandidate().getEmail().equalsIgnoreCase(candUser.getEmail());
            if (!isOwner) {
                throw new AccessDeniedException("Access denied: You can only view your own resume");
            }
        }

        ResumeUploadResponse resp = new ResumeUploadResponse();
        resp.setResumeId(resume.getId());
        resp.setCandidateId(resume.getCandidate().getId());
        resp.setCandidateName(resume.getCandidate().getFullName());
        resp.setCandidateEmail(resume.getCandidate().getEmail());
        resp.setFileName(resume.getFileName());
        resp.setFileSize(resume.getFileSize());
        resp.setRawText(resume.getRawText());
        resp.setCreatedAt(resume.getCreatedAt());

        ResumeAnalysisResult analysis = new ResumeAnalysisResult();
        analysis.setCandidateName(resume.getCandidate().getFullName());
        analysis.setEmail(resume.getCandidate().getEmail());
        analysis.setPhone(resume.getCandidate().getPhone());
        analysis.setCurrentTitle(resume.getCandidate().getCurrentTitle());
        analysis.setYearsOfExperience(resume.getCandidate().getYearsOfExperience());
        analysis.setLocation(resume.getCandidate().getLocation());
        analysis.setSummary(resume.getCandidate().getSummary());

        try {
            if (resume.getParsedSkills() != null) {
                analysis.setSkills(objectMapper.readValue(resume.getParsedSkills(), new TypeReference<List<String>>() {}));
            }
            if (resume.getParsedExperience() != null) {
                analysis.setExperience(objectMapper.readValue(resume.getParsedExperience(), new TypeReference<>() {}));
            }
            if (resume.getParsedEducation() != null) {
                analysis.setEducation(objectMapper.readValue(resume.getParsedEducation(), new TypeReference<>() {}));
            }
            if (resume.getParsedProjects() != null) {
                analysis.setProjects(objectMapper.readValue(resume.getParsedProjects(), new TypeReference<>() {}));
            }
            if (resume.getStrengths() != null) {
                analysis.setStrengths(objectMapper.readValue(resume.getStrengths(), new TypeReference<List<String>>() {}));
            }
            if (resume.getMissingInfo() != null) {
                analysis.setMissingInfo(objectMapper.readValue(resume.getMissingInfo(), new TypeReference<List<String>>() {}));
            }
        } catch (Exception ignored) {
        }

        resp.setAnalysis(analysis);
        return resp;
    }
}
