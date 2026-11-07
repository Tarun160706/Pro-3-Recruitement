package com.talentiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.JobMatchRequest;
import com.talentiq.dto.JobMatchResponse;
import com.talentiq.entity.Application;
import com.talentiq.entity.Candidate;
import com.talentiq.entity.Job;
import com.talentiq.entity.JobMatch;
import com.talentiq.entity.Resume;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.ApplicationRepository;
import com.talentiq.repository.CandidateRepository;
import com.talentiq.repository.JobMatchRepository;
import com.talentiq.repository.JobRepository;
import com.talentiq.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobMatchService {

    private final JobMatchRepository jobMatchRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final GeminiAiService geminiAiService;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public JobMatchService(JobMatchRepository jobMatchRepository,
                           JobRepository jobRepository,
                           CandidateRepository candidateRepository,
                           ResumeRepository resumeRepository,
                           ApplicationRepository applicationRepository,
                           GeminiAiService geminiAiService,
                           ActivityLogService activityLogService,
                           ObjectMapper objectMapper) {
        this.jobMatchRepository = jobMatchRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
        this.applicationRepository = applicationRepository;
        this.geminiAiService = geminiAiService;
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JobMatchResponse evaluateMatch(JobMatchRequest request) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + request.getCandidateId()));

        Resume resume;
        if (request.getResumeId() != null) {
            resume = resumeRepository.findById(request.getResumeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + request.getResumeId()));
        } else {
            resume = resumeRepository.findFirstByCandidateIdOrderByCreatedAtDesc(candidate.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("No resume found for candidate: " + candidate.getFullName()));
        }

        List<String> skills = Collections.emptyList();
        try {
            if (resume.getParsedSkills() != null) {
                skills = objectMapper.readValue(resume.getParsedSkills(), new TypeReference<List<String>>() {});
            }
        } catch (Exception ignored) {
        }

        JobMatchResponse evaluated = geminiAiService.evaluateJobMatch(job, resume.getRawText(), skills);

        JobMatch jobMatch = new JobMatch();
        jobMatch.setJob(job);
        jobMatch.setCandidate(candidate);
        jobMatch.setResume(resume);
        jobMatch.setOverallScore(evaluated.getOverallScore());
        jobMatch.setSkillsScore(evaluated.getSkillsScore());
        jobMatch.setExperienceScore(evaluated.getExperienceScore());
        jobMatch.setExplanation(evaluated.getExplanation());

        try {
            jobMatch.setMatchedSkills(objectMapper.writeValueAsString(evaluated.getMatchedSkills()));
            jobMatch.setMissingSkills(objectMapper.writeValueAsString(evaluated.getMissingSkills()));
            jobMatch.setResumeEvidence(objectMapper.writeValueAsString(evaluated.getResumeEvidence()));
        } catch (Exception ignored) {
            jobMatch.setMatchedSkills("[]");
            jobMatch.setMissingSkills("[]");
            jobMatch.setResumeEvidence("[]");
        }

        JobMatch savedMatch = jobMatchRepository.save(jobMatch);

        // Update application if exists
        Optional<Application> applicationOpt = applicationRepository.findByJobIdAndCandidateId(job.getId(), candidate.getId());
        if (applicationOpt.isPresent()) {
            Application app = applicationOpt.get();
            if ("APPLIED".equals(app.getStatus())) {
                app.setStatus("SCREENED");
            }
            app.setResume(resume);
            applicationRepository.save(app);
        }

        activityLogService.log("MATCH_EVALUATED", "AI Job Match Evaluated",
                "Evaluated " + candidate.getFullName() + " for '" + job.getTitle() + "' with match score of " + evaluated.getOverallScore() + "%.");

        evaluated.setId(savedMatch.getId());
        evaluated.setCandidateId(candidate.getId());
        evaluated.setCandidateName(candidate.getFullName());
        evaluated.setCreatedAt(savedMatch.getCreatedAt());

        return evaluated;
    }

    @Transactional(readOnly = true)
    public List<JobMatchResponse> getMatchesByJob(Long jobId) {
        return jobMatchRepository.findByJobId(jobId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobMatchResponse getMatchById(Long id) {
        JobMatch match = jobMatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job match assessment not found with id: " + id));
        return mapToResponse(match);
    }

    private JobMatchResponse mapToResponse(JobMatch match) {
        JobMatchResponse resp = new JobMatchResponse();
        resp.setId(match.getId());
        resp.setJobId(match.getJob().getId());
        resp.setJobTitle(match.getJob().getTitle());
        resp.setCandidateId(match.getCandidate().getId());
        resp.setCandidateName(match.getCandidate().getFullName());
        resp.setOverallScore(match.getOverallScore());
        resp.setSkillsScore(match.getSkillsScore());
        resp.setExperienceScore(match.getExperienceScore());
        resp.setExplanation(match.getExplanation());
        resp.setCreatedAt(match.getCreatedAt());

        try {
            resp.setMatchedSkills(objectMapper.readValue(match.getMatchedSkills(), new TypeReference<List<String>>() {}));
            resp.setMissingSkills(objectMapper.readValue(match.getMissingSkills(), new TypeReference<List<String>>() {}));
            resp.setResumeEvidence(objectMapper.readValue(match.getResumeEvidence(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            resp.setMatchedSkills(Collections.emptyList());
            resp.setMissingSkills(Collections.emptyList());
            resp.setResumeEvidence(Collections.emptyList());
        }

        return resp;
    }
}
