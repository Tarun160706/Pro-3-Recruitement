package com.talentiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.RegisterRequest;
import com.talentiq.entity.Role;
import com.talentiq.entity.User;
import com.talentiq.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidateDataIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private CandidateRepository candidateRepository;

    private User candidateUser;
    private String candidateToken;
    private User recruiterUser;
    private String recruiterToken;

    @BeforeEach
    void setUp() {
        candidateUser = testAuthHelper.createVerifiedCandidate("alex.verified@talentiq.ai", "Alex Rivera");
        candidateToken = testAuthHelper.generateToken(candidateUser);

        // Associate existing demo candidate profile with candidateUser if present
        candidateRepository.findByEmail("candidate@talentiq.ai").ifPresent(c -> {
            candidateRepository.findByUserId(candidateUser.getId()).ifPresent(existing -> {
                if (!existing.getId().equals(c.getId())) {
                    candidateRepository.delete(existing);
                }
            });
            c.setUser(candidateUser);
            candidateRepository.save(c);
        });

        recruiterUser = testAuthHelper.createVerifiedRecruiter("sarah.verified@talentiq.ai", "Sarah Jenkins");
        recruiterToken = testAuthHelper.generateToken(recruiterUser);
    }

    @Test
    @DisplayName("Candidate A views their own profile via /api/candidate/me/profile")
    void testCandidateCanViewOwnProfile() throws Exception {
        mockMvc.perform(get("/api/candidate/me/profile")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alex Rivera"));
    }

    @Test
    @DisplayName("Candidate A views their own applications via /api/candidate/me/applications")
    void testCandidateCanViewOwnApplications() throws Exception {
        mockMvc.perform(get("/api/candidate/me/applications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].candidateName").value("Alex Rivera"));
    }

    @Test
    @DisplayName("Candidate A views their own interview sessions via /api/candidate/me/interviews")
    void testCandidateCanViewOwnInterviews() throws Exception {
        mockMvc.perform(get("/api/candidate/me/interviews")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].candidateName").value("Alex Rivera"));
    }

    @Test
    @DisplayName("Candidate B profile is isolated and distinct from Candidate A")
    void testCandidateProfileIsolation() throws Exception {
        User candidateB = testAuthHelper.createVerifiedCandidate("candidate.b@isolate.io", "Candidate B User");
        String candidateBToken = testAuthHelper.generateToken(candidateB);

        mockMvc.perform(get("/api/candidate/me/profile")
                        .header("Authorization", "Bearer " + candidateBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Candidate B User"))
                .andExpect(jsonPath("$.email").value("candidate.b@isolate.io"));

        mockMvc.perform(get("/api/candidate/me/applications")
                        .header("Authorization", "Bearer " + candidateBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/candidate/me/interviews")
                        .header("Authorization", "Bearer " + candidateBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Candidate cannot access another candidate's private resume (403 Forbidden)")
    void testCandidateCannotAccessOtherCandidateResume() throws Exception {
        User candidateB = testAuthHelper.createVerifiedCandidate("candidate.b@isolate.io", "Candidate B User");
        String candidateBToken = testAuthHelper.generateToken(candidateB);

        mockMvc.perform(get("/api/resumes/1")
                        .header("Authorization", "Bearer " + candidateBToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Access denied")));
    }

    @Test
    @DisplayName("Candidate cannot access another candidate's private interview session (403 Forbidden)")
    void testCandidateCannotAccessOtherCandidateInterview() throws Exception {
        User candidateB = testAuthHelper.createVerifiedCandidate("candidate.b@isolate.io", "Candidate B User");
        String candidateBToken = testAuthHelper.generateToken(candidateB);

        mockMvc.perform(get("/api/interviews/1")
                        .header("Authorization", "Bearer " + candidateBToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Access denied")));
    }

    @Test
    @DisplayName("Candidate is Forbidden (403) from listing all job interview sessions")
    void testCandidateBlockedFromJobInterviewsList() throws Exception {
        mockMvc.perform(get("/api/interviews/job/1")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Recruiter has administrative clearance to inspect any candidate interview or resume")
    void testRecruiterCanViewAnyInterviewAndResume() throws Exception {
        mockMvc.perform(get("/api/interviews/1")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/api/resumes/1")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId").value(1));
    }
}
