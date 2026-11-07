package com.talentiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.GenerateInterviewRequest;
import com.talentiq.dto.InterviewFeedbackRequest;
import com.talentiq.dto.InterviewQuestionDTO;
import com.talentiq.dto.InterviewSessionResponse;
import com.talentiq.entity.*;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final AuthService authService;
    private final GeminiAiService geminiAiService;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public InterviewService(InterviewSessionRepository sessionRepository,
                            InterviewQuestionRepository questionRepository,
                            JobRepository jobRepository,
                            CandidateRepository candidateRepository,
                            ResumeRepository resumeRepository,
                            ApplicationRepository applicationRepository,
                            AuthService authService,
                            GeminiAiService geminiAiService,
                            ActivityLogService activityLogService,
                            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
        this.applicationRepository = applicationRepository;
        this.authService = authService;
        this.geminiAiService = geminiAiService;
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public InterviewSessionResponse generateInterview(GenerateInterviewRequest request) {
        User recruiter = authService.getCurrentAuthenticatedUser();

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + request.getCandidateId()));

        Optional<Resume> resumeOpt = resumeRepository.findFirstByCandidateIdOrderByCreatedAtDesc(candidate.getId());
        String resumeText = resumeOpt.map(Resume::getRawText).orElse("");

        List<String> skills = Collections.emptyList();
        if (resumeOpt.isPresent() && resumeOpt.get().getParsedSkills() != null) {
            try {
                skills = objectMapper.readValue(resumeOpt.get().getParsedSkills(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
            }
        }

        List<InterviewQuestionDTO> generatedQuestions = geminiAiService.generateInterviewQuestions(
                job, candidate.getFullName(), resumeText, skills);

        LocalDateTime scheduledAt = request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now().plusDays(2);
        InterviewSession session = new InterviewSession(job, candidate, recruiter, request.getTitle(), scheduledAt);

        for (InterviewQuestionDTO qDto : generatedQuestions) {
            InterviewQuestion q = new InterviewQuestion(
                    qDto.getDifficulty(),
                    qDto.getCategory(),
                    qDto.getQuestion(),
                    qDto.getExpectedAnswer(),
                    qDto.getFollowUp()
            );
            session.addQuestion(q);
        }

        InterviewSession savedSession = sessionRepository.save(session);

        // Update application status to INTERVIEW_SCHEDULED
        Optional<Application> appOpt = applicationRepository.findByJobIdAndCandidateId(job.getId(), candidate.getId());
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            app.setStatus("INTERVIEW_SCHEDULED");
            app.setStage("Technical Interview");
            applicationRepository.save(app);
        }

        activityLogService.log("INTERVIEW_GENERATED", "Technical Interview Questions Generated",
                "AI generated " + generatedQuestions.size() + " technical questions for " + candidate.getFullName() + " ('" + job.getTitle() + "').");

        return mapToResponse(savedSession);
    }

    @Transactional
    public InterviewSessionResponse submitFeedback(Long sessionId, InterviewFeedbackRequest request) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found with id: " + sessionId));

        session.setOverallFeedback(request.getOverallFeedback());
        session.setCompetencyScore(request.getCompetencyScore());
        session.setStatus("COMPLETED");

        // Update question ratings if provided
        if (request.getQuestionRatings() != null) {
            for (InterviewQuestion q : session.getQuestions()) {
                if (request.getQuestionRatings().containsKey(q.getId())) {
                    q.setRecruiterRating(request.getQuestionRatings().get(q.getId()));
                }
                if (request.getCandidateAnswers() != null && request.getCandidateAnswers().containsKey(q.getId())) {
                    q.setCandidateAnswer(request.getCandidateAnswers().get(q.getId()));
                }
            }
        }

        // Generate AI-assisted recommendations
        List<String> jobSkills = Collections.emptyList();
        try {
            if (session.getJob().getRequiredSkills() != null) {
                jobSkills = objectMapper.readValue(session.getJob().getRequiredSkills(), new TypeReference<List<String>>() {});
            }
        } catch (Exception ignored) {
        }

        Map<String, Object> aiRecs = geminiAiService.generateFeedbackRecommendations(
                request.getOverallFeedback(), request.getCompetencyScore(), jobSkills);

        try {
            if (request.getStrengths() != null && !request.getStrengths().isEmpty()) {
                session.setStrengths(objectMapper.writeValueAsString(request.getStrengths()));
            } else {
                session.setStrengths(objectMapper.writeValueAsString(aiRecs.get("strengths")));
            }

            if (request.getImprovementAreas() != null && !request.getImprovementAreas().isEmpty()) {
                session.setImprovementAreas(objectMapper.writeValueAsString(request.getImprovementAreas()));
            } else {
                session.setImprovementAreas(objectMapper.writeValueAsString(aiRecs.get("improvementAreas")));
            }

            if (request.getLearningRecommendations() != null && !request.getLearningRecommendations().isEmpty()) {
                session.setLearningRecommendations(objectMapper.writeValueAsString(request.getLearningRecommendations()));
            } else {
                session.setLearningRecommendations(objectMapper.writeValueAsString(aiRecs.get("learningRecommendations")));
            }
        } catch (Exception ignored) {
        }

        InterviewSession updated = sessionRepository.save(session);
        activityLogService.log("INTERVIEW_COMPLETED", "Interview Evaluation Completed",
                "Completed interview feedback for " + updated.getCandidate().getFullName() + " (Score: " + updated.getCompetencyScore() + "/100).");

        return mapToResponse(updated);
    }

    @Transactional
    public InterviewQuestionDTO updateQuestion(Long questionId, InterviewQuestionDTO updateDto) {
        InterviewQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        if (updateDto.getQuestion() != null) question.setQuestion(updateDto.getQuestion());
        if (updateDto.getExpectedAnswer() != null) question.setExpectedAnswer(updateDto.getExpectedAnswer());
        if (updateDto.getFollowUp() != null) question.setFollowUp(updateDto.getFollowUp());
        if (updateDto.getCategory() != null) question.setCategory(updateDto.getCategory());
        if (updateDto.getDifficulty() != null) question.setDifficulty(updateDto.getDifficulty());
        if (updateDto.getRecruiterRating() != null) question.setRecruiterRating(updateDto.getRecruiterRating());
        if (updateDto.getCandidateAnswer() != null) question.setCandidateAnswer(updateDto.getCandidateAnswer());

        InterviewQuestion saved = questionRepository.save(question);
        return new InterviewQuestionDTO(saved);
    }

    @Transactional(readOnly = true)
    public InterviewSessionResponse getInterviewById(Long id) {
        InterviewSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found with id: " + id));

        Optional<User> authUserOpt = authService.getOptionalAuthenticatedUser();
        if (authUserOpt.isPresent() && authUserOpt.get().getRole() == Role.ROLE_CANDIDATE) {
            User candUser = authUserOpt.get();
            boolean isOwner = (session.getCandidate().getUser() != null && session.getCandidate().getUser().getId().equals(candUser.getId()))
                    || session.getCandidate().getEmail().equalsIgnoreCase(candUser.getEmail());
            if (!isOwner) {
                throw new AccessDeniedException("Access denied: You can only view your own interview session");
            }
        }

        return mapToResponse(session);
    }

    @Transactional(readOnly = true)
    public List<InterviewSessionResponse> getInterviewsByJob(Long jobId) {
        return sessionRepository.findByJobId(jobId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewSessionResponse> getInterviewsByCandidate(Long candidateId) {
        Optional<User> authUserOpt = authService.getOptionalAuthenticatedUser();
        if (authUserOpt.isPresent() && authUserOpt.get().getRole() == Role.ROLE_CANDIDATE) {
            User candUser = authUserOpt.get();
            Candidate targetCandidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));
            boolean isOwner = (targetCandidate.getUser() != null && targetCandidate.getUser().getId().equals(candUser.getId()))
                    || targetCandidate.getEmail().equalsIgnoreCase(candUser.getEmail());
            if (!isOwner) {
                throw new AccessDeniedException("Access denied: You cannot view another candidate's interviews");
            }
        }
        return sessionRepository.findByCandidateId(candidateId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public InterviewSessionResponse mapToResponse(InterviewSession session) {
        InterviewSessionResponse resp = new InterviewSessionResponse();
        resp.setId(session.getId());
        resp.setJobId(session.getJob().getId());
        resp.setJobTitle(session.getJob().getTitle());
        resp.setCandidateId(session.getCandidate().getId());
        resp.setCandidateName(session.getCandidate().getFullName());
        resp.setCandidateEmail(session.getCandidate().getEmail());
        resp.setRecruiterId(session.getRecruiter().getId());
        resp.setRecruiterName(session.getRecruiter().getFullName());
        resp.setTitle(session.getTitle());
        resp.setStatus(session.getStatus());
        resp.setScheduledAt(session.getScheduledAt());
        resp.setOverallFeedback(session.getOverallFeedback());
        resp.setCompetencyScore(session.getCompetencyScore());
        resp.setCreatedAt(session.getCreatedAt());

        try {
            if (session.getStrengths() != null) {
                resp.setStrengths(objectMapper.readValue(session.getStrengths(), new TypeReference<List<String>>() {}));
            }
            if (session.getImprovementAreas() != null) {
                resp.setImprovementAreas(objectMapper.readValue(session.getImprovementAreas(), new TypeReference<List<String>>() {}));
            }
            if (session.getLearningRecommendations() != null) {
                resp.setLearningRecommendations(objectMapper.readValue(session.getLearningRecommendations(), new TypeReference<List<String>>() {}));
            }
        } catch (Exception ignored) {
        }

        List<InterviewQuestionDTO> qDtos = session.getQuestions().stream()
                .map(InterviewQuestionDTO::new)
                .collect(Collectors.toList());
        resp.setQuestions(qDtos);

        return resp;
    }
}
