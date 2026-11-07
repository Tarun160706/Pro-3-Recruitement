package com.talentiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.JobRequest;
import com.talentiq.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JobManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper testAuthHelper;

    private String recruiterToken;
    private String candidateToken;

    @BeforeEach
    void setUp() {
        User recruiter = testAuthHelper.createVerifiedRecruiter("recruiter.jobs@talentiq.ai", "Job Manager Sarah");
        recruiterToken = testAuthHelper.generateToken(recruiter);

        User candidate = testAuthHelper.createVerifiedCandidate("candidate.jobs@talentiq.ai", "Candidate Alex");
        candidateToken = testAuthHelper.generateToken(candidate);
    }

    @Test
    @DisplayName("Recruiter successfully creates a new Job opening (201 Created)")
    void testCreateJobSuccess() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle("Staff Site Reliability Engineer");
        req.setDepartment("Infrastructure & Cloud");
        req.setLocation("Remote (Global)");
        req.setJobType("FULL_TIME");
        req.setExperienceMin(6);
        req.setExperienceMax(12);
        req.setRequiredSkills(List.of("Kubernetes", "Terraform", "Go", "AWS", "Prometheus"));
        req.setDescription("Lead mission-critical cloud platform resilience and automated self-healing infrastructure.");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Staff Site Reliability Engineer"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.requiredSkills", hasItems("Kubernetes", "Terraform", "Go")));
    }

    @Test
    @DisplayName("Job creation with missing required fields returns 400 Bad Request")
    void testCreateJobValidationFailure() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle(""); // Blank title violates @NotBlank
        req.setDescription("");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    @DisplayName("Public user can browse all active jobs without authentication")
    void testPublicBrowseJobs() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").isString())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Search jobs by title keyword and department filter")
    void testSearchAndFilterJobs() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .param("search", "Java")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", containsString("Java")));

        mockMvc.perform(get("/api/jobs")
                        .param("department", "Platform Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].department").value("Platform Engineering"));
    }

    @Test
    @DisplayName("Get specific job opening by ID returns 200 OK")
    void testGetJobById() throws Exception {
        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").isString());
    }

    @Test
    @DisplayName("Get non-existent job ID returns 404 Not Found")
    void testGetNonExistentJob() throws Exception {
        mockMvc.perform(get("/api/jobs/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("Recruiter successfully updates existing job opening (PUT /api/jobs/{id})")
    void testUpdateJob() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle("Lead Java & Distributed Systems Engineer");
        req.setDepartment("Platform Engineering");
        req.setLocation("San Francisco, CA");
        req.setJobType("FULL_TIME");
        req.setExperienceMin(5);
        req.setExperienceMax(10);
        req.setRequiredSkills(List.of("Java", "Spring Boot", "Kafka", "PostgreSQL"));
        req.setDescription("Updated description with Kafka integration focus.");
        req.setStatus("ACTIVE");

        mockMvc.perform(put("/api/jobs/1")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lead Java & Distributed Systems Engineer"))
                .andExpect(jsonPath("$.requiredSkills", hasItem("Kafka")));
    }

    @Test
    @DisplayName("Recruiter archives job status (PATCH /api/jobs/{id}/status)")
    void testPatchJobStatus() throws Exception {
        mockMvc.perform(patch("/api/jobs/1/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("status", "ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("Recruiter deletes job opening (DELETE /api/jobs/{id})")
    void testDeleteJob() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle("Temporary Contractor Job");
        req.setRequiredSkills(List.of("Go"));
        req.setDescription("Temporary role to be deleted.");

        MvcResult createRes = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        long createdId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // Delete the created job
        mockMvc.perform(delete("/api/jobs/" + createdId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isNoContent());

        // Verify it is gone
        mockMvc.perform(get("/api/jobs/" + createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ROLE_CANDIDATE is forbidden from creating job openings")
    void testCandidateBlockedFromCreatingJob() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle("Hacked Title");
        req.setRequiredSkills(List.of("Java"));
        req.setDescription("Unauthorized attempt.");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
