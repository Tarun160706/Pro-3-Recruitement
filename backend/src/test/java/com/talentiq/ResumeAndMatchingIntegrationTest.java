package com.talentiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.JobMatchRequest;
import com.talentiq.dto.JobMatchResponse;
import com.talentiq.dto.ResumeAnalysisResult;
import com.talentiq.entity.User;
import com.talentiq.service.GeminiAiService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResumeAndMatchingIntegrationTest {

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
        User recruiter = testAuthHelper.createVerifiedRecruiter("recruiter.resumes@talentiq.ai", "Sarah Recruiter");
        recruiterToken = testAuthHelper.generateToken(recruiter);
    }

    @Test
    @DisplayName("Upload plaintext resume extracts competencies and returns 201 Created")
    void testUploadPlainTextResume() throws Exception {
        String resumeContent = """
            Elena Rostova
            elena.rostova@tech.io | +1-555-0999 | Chicago, IL
            SUMMARY: Senior Full Stack Engineer with 4 years building scalable microservices in Java, Spring Boot, React, and PostgreSQL.
            SKILLS: Java, Spring Boot, React, TypeScript, Docker, PostgreSQL, REST APIs
            EXPERIENCE:
            Full Stack Lead at Nexus Systems (2022 - Present): Built high concurrency Spring Boot APIs and React frontends.
            EDUCATION:
            B.S. in Computer Science, University of Illinois (2020)
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "elena_resume.txt",
                "text/plain",
                resumeContent.getBytes()
        );

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("candidateId", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumeId").isNumber())
                .andExpect(jsonPath("$.candidateName").value("Priya Sharma"))
                .andExpect(jsonPath("$.parsedSkills", hasItems("Java", "Spring Boot", "React", "PostgreSQL")))
                .andExpect(jsonPath("$.rawTextLength", greaterThan(50)));
    }

    @Test
    @DisplayName("Upload binary PDF resume extracted correctly via Apache PDFBox")
    void testUploadPdfResume() throws Exception {
        byte[] pdfBytes = generateTestPdf(
                "Marcus Vance - Senior Backend Engineer",
                "Skills: Java, Spring Boot, Kafka, Docker, Kubernetes, AWS, PostgreSQL",
                "Experience: 6 years architecting high throughput streaming data pipelines."
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "marcus_vance_cv.pdf",
                "application/pdf",
                pdfBytes
        );

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("candidateId", "3"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumeId").isNumber())
                .andExpect(jsonPath("$.fileName").value("marcus_vance_cv.pdf"))
                .andExpect(jsonPath("$.candidateName").value("Marcus Vance"))
                .andExpect(jsonPath("$.parsedSkills", hasItems("Java", "Spring Boot", "Kafka", "Docker", "Kubernetes")));
    }

    @Test
    @DisplayName("Upload unsupported file format (.exe) returns 400 Bad Request")
    void testUploadUnsupportedFormatFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "MZ executable header dummy bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("candidateId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Unsupported file type")));
    }

    @Test
    @DisplayName("AI Job Match evaluates Alex Rivera against Job 1 with >85% score")
    void testAiJobMatchEvaluation() throws Exception {
        JobMatchRequest req = new JobMatchRequest();
        req.setJobId(1L);
        req.setCandidateId(1L);

        mockMvc.perform(post("/api/matches/evaluate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore", greaterThanOrEqualTo(80)))
                .andExpect(jsonPath("$.skillsScore", greaterThanOrEqualTo(80)))
                .andExpect(jsonPath("$.matchedSkills", hasItems("Java", "Spring Boot", "PostgreSQL")))
                .andExpect(jsonPath("$.explanation").isString())
                .andExpect(jsonPath("$.resumeEvidence", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Evaluate Job Match using mocked Gemini LLM structured output")
    void testJobMatchWithMockedGemini() throws Exception {
        JobMatchResponse mockLlmMatch = new JobMatchResponse(
                96,
                98,
                94,
                List.of("Java", "Spring Boot", "PostgreSQL", "REST APIs", "Microservices", "Docker"),
                List.of(),
                "Alex Rivera is an exceptional match with verified 5.5 years deep Spring Boot microservice architecture depth.",
                List.of("Exceeds minimum experience requirement", "Demonstrated PostgreSQL query optimization experience")
        );

        doReturn(mockLlmMatch).when(geminiAiService).evaluateJobMatch(any(), any(), any());

        JobMatchRequest req = new JobMatchRequest();
        req.setJobId(1L);
        req.setCandidateId(1L);

        mockMvc.perform(post("/api/matches/evaluate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").value(96))
                .andExpect(jsonPath("$.skillsScore").value(98))
                .andExpect(jsonPath("$.experienceScore").value(94))
                .andExpect(jsonPath("$.matchedSkills", hasSize(6)))
                .andExpect(jsonPath("$.missingSkills", hasSize(0)))
                .andExpect(jsonPath("$.resumeEvidence", hasSize(2)));
    }

    @Test
    @DisplayName("Heuristic fallback activates cleanly when Gemini API is unavailable")
    void testHeuristicFallbackOnAiFailure() throws Exception {
        doAnswer(invocation -> geminiAiService.localEvaluateJobMatch(
                invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2)
        )).when(geminiAiService).evaluateJobMatch(any(), any(), any());

        JobMatchRequest req = new JobMatchRequest();
        req.setJobId(1L);
        req.setCandidateId(1L);

        mockMvc.perform(post("/api/matches/evaluate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore", greaterThanOrEqualTo(50)))
                .andExpect(jsonPath("$.matchedSkills").isArray())
                .andExpect(jsonPath("$.explanation", containsString("core competencies")));
    }

    @Test
    @DisplayName("Evaluating match with non-existent job ID returns 404 Not Found")
    void testEvaluateMatchJobNotFound() throws Exception {
        JobMatchRequest req = new JobMatchRequest();
        req.setJobId(99999L);
        req.setCandidateId(1L);

        mockMvc.perform(post("/api/matches/evaluate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Evaluating match with non-existent candidate ID returns 404 Not Found")
    void testEvaluateMatchCandidateNotFound() throws Exception {
        JobMatchRequest req = new JobMatchRequest();
        req.setJobId(1L);
        req.setCandidateId(99999L);

        mockMvc.perform(post("/api/matches/evaluate")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    private byte[] generateTestPdf(String... lines) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.setLeading(16f);

                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }
}
