package com.talentiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.JobRequest;
import com.talentiq.dto.JobResponse;
import com.talentiq.entity.Job;
import com.talentiq.entity.User;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.ApplicationRepository;
import com.talentiq.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final AuthService authService;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public JobService(JobRepository jobRepository,
                      ApplicationRepository applicationRepository,
                      AuthService authService,
                      ActivityLogService activityLogService,
                      ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.authService = authService;
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JobResponse createJob(JobRequest request) {
        User recruiter = authService.getCurrentAuthenticatedUser();

        String skillsJson = serializeSkills(request.getRequiredSkills());

        Job job = new Job(
                recruiter,
                request.getTitle(),
                request.getDepartment(),
                request.getLocation(),
                request.getJobType(),
                request.getExperienceMin(),
                request.getExperienceMax(),
                skillsJson,
                request.getDescription(),
                request.getStatus()
        );

        Job saved = jobRepository.save(job);
        activityLogService.log("JOB_CREATED", "Job Opening Published",
                "Job '" + saved.getTitle() + "' published for " + saved.getDepartment() + " department.");

        return mapToResponse(saved);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + id));

        job.setTitle(request.getTitle());
        job.setDepartment(request.getDepartment());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setExperienceMin(request.getExperienceMin());
        job.setExperienceMax(request.getExperienceMax());
        job.setRequiredSkills(serializeSkills(request.getRequiredSkills()));
        job.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        Job updated = jobRepository.save(job);
        activityLogService.log("JOB_UPDATED", "Job Updated", "Job '" + updated.getTitle() + "' was modified.");
        return mapToResponse(updated);
    }

    @Transactional
    public JobResponse updateStatus(Long id, String status) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + id));
        job.setStatus(status.toUpperCase());
        Job updated = jobRepository.save(job);
        activityLogService.log("JOB_STATUS_CHANGED", "Job Status Changed",
                "Job '" + updated.getTitle() + "' status changed to " + updated.getStatus());
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + id));
        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs(String status, String department, String search) {
        List<Job> jobs;
        if (status == null && department == null && search == null) {
            jobs = jobRepository.findAll();
        } else {
            jobs = jobRepository.searchJobs(status, department, search);
        }
        return jobs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        jobRepository.delete(job);
        activityLogService.log("JOB_DELETED", "Job Deleted", "Job '" + job.getTitle() + "' was removed.");
    }

    public JobResponse mapToResponse(Job job) {
        JobResponse resp = new JobResponse();
        resp.setId(job.getId());
        resp.setRecruiterId(job.getRecruiter().getId());
        resp.setRecruiterName(job.getRecruiter().getFullName());
        resp.setTitle(job.getTitle());
        resp.setDepartment(job.getDepartment());
        resp.setLocation(job.getLocation());
        resp.setJobType(job.getJobType());
        resp.setExperienceMin(job.getExperienceMin());
        resp.setExperienceMax(job.getExperienceMax());
        resp.setRequiredSkills(deserializeSkills(job.getRequiredSkills()));
        resp.setDescription(job.getDescription());
        resp.setStatus(job.getStatus());
        resp.setApplicantCount(applicationRepository.findByJobId(job.getId()).size());
        resp.setCreatedAt(job.getCreatedAt());
        return resp;
    }

    private String serializeSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(skills);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<String> deserializeSkills(String json) {
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
