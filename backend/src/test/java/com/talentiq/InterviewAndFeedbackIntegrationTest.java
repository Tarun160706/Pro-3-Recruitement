package com.talentiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.GenerateInterviewRequest;
import com.talentiq.dto.InterviewFeedbackRequest;
import com.talentiq.dto.InterviewQuestionDTO;
import com.talentiq.entity.Job;
import com.talentiq.entity.User;
import com.talentiq.service.GeminiAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InterviewAndFeedbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @SpyBean
    private GeminiAiService geminiAiService;

    private String recruiterToken;

    @BeforeEach
    void setUp() {
        User recruiter = testAuthHelper.createVerifiedRecruiter("recruiter.interviews@talentiq.ai", "Sarah Recruiter");
        recruiterToken = testAuthHelper.generateToken(recruiter);
    }

    @Test
    @DisplayName("Recruiter generates tailored interview questions across Beginner, Intermediate, and Advanced tiers")
    void testGenerateInterviewQuestions() throws Exception {
        GenerateInterviewRequest req = new GenerateInterviewRequest();
        req.setJobId(1L);
        req.setCandidateId(1L);
        req.setTitle("Distributed Systems Technical Round");

        mockMvc.perform(post("/api/interviews/generate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Distributed Systems Technical Round"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.candidateName").value("Alex Rivera"))
                .andExpect(jsonPath("$.questions", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.questions[0].difficulty").exists())
                .andExpect(jsonPath("$.questions[0].expectedAnswer").exists());
    }

    @Test
    @DisplayName("Generate interview questions using mocked Gemini AI response")
    void testGenerateInterviewWithMockedGemini() throws Exception {
        List<InterviewQuestionDTO> mockQuestions = List.of(
                new InterviewQuestionDTO("BEGINNER", "Core Java",
                        "Explain Java memory model and Garbage Collection.",
                        "Expected understanding of Eden, Survivor, Tenured spaces.",
                        "Follow up: G1 GC pause time target tuning."),
                new InterviewQuestionDTO("INTERMEDIATE", "Spring Concurrency",
                        "How does Spring manage transaction propagation with @Transactional?",
                        "Expected explanation of proxying and Propagation.REQUIRES_NEW vs REQUIRED.",
                        "Follow up: What occurs during self-invocation?"),
                new InterviewQuestionDTO("ADVANCED", "PostgreSQL High Availability",
                        "Design replication with streaming replication and connection pooling using PgBouncer.",
                        "Expected discussion of write ahead logging, WAL archiving, and failover.",
                        "Follow up: How to prevent read replica lag?")
        );

        doReturn(mockQuestions).when(geminiAiService).generateInterviewQuestions(any(Job.class), any(), any(), any());

        GenerateInterviewRequest req = new GenerateInterviewRequest();
        req.setJobId(1L);
        req.setCandidateId(1L);
        req.setTitle("Mocked Gemini Architecture Round");

        mockMvc.perform(post("/api/interviews/generate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Mocked Gemini Architecture Round"))
                .andExpect(jsonPath("$.questions", hasSize(3)))
                .andExpect(jsonPath("$.questions[0].category").value("Core Java"))
                .andExpect(jsonPath("$.questions[1].category").value("Spring Concurrency"))
                .andExpect(jsonPath("$.questions[2].category").value("PostgreSQL High Availability"));
    }

    @Test
    @DisplayName("Recruiter submits evaluation feedback and AI generates structured analysis")
    void testSubmitInterviewFeedback() throws Exception {
        Map<String, Object> mockFeedbackAnalysis = Map.of(
                "strengths", List.of("Spring Boot Internals", "Relational Database Indexing"),
                "improvementAreas", List.of("Could expand on distributed consensus algorithms"),
                "learningRecommendations", List.of("Review Raft consensus protocol", "Explore OpenTelemetry tracing")
        );

        doReturn(mockFeedbackAnalysis).when(geminiAiService).generateFeedbackRecommendations(any(), any(), any());

        InterviewFeedbackRequest req = new InterviewFeedbackRequest();
        req.setOverallFeedback("Candidate delivered clear, structured answers with strong mastery of PostgreSQL indexing.");
        req.setCompetencyScore(93);

        mockMvc.perform(post("/api/interviews/1/feedback")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.competencyScore").value(93))
                .andExpect(jsonPath("$.strengths", hasItem("Spring Boot Internals")))
                .andExpect(jsonPath("$.learningRecommendations", hasItem(containsString("OpenTelemetry"))));
    }

    @Test
    @DisplayName("Interview generation with invalid Job ID returns 404 Not Found")
    void testGenerateInterviewJobNotFound() throws Exception {
        GenerateInterviewRequest req = new GenerateInterviewRequest();
        req.setJobId(99999L);
        req.setCandidateId(1L);

        mockMvc.perform(post("/api/interviews/generate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
