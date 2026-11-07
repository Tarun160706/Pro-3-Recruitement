package com.talentiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.*;
import com.talentiq.entity.Role;
import com.talentiq.entity.User;
import com.talentiq.entity.VerificationStatus;
import com.talentiq.repository.UserRepository;
import com.talentiq.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthAndRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Pre-seeded demo recruiter account is not accepted for login")
    void testDemoRecruiterAccountRejected() throws Exception {
        AuthRequest req = new AuthRequest("recruiter@talentiq.ai", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Pre-seeded demo candidate account is not accepted for login")
    void testDemoCandidateAccountRejected() throws Exception {
        AuthRequest req = new AuthRequest("candidate@talentiq.ai", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Candidate registration saves unverified status and dispatches verification email")
    void testCandidateRegistrationSuccess() throws Exception {
        CandidateRegisterRequest req = new CandidateRegisterRequest(
                "Alice Smith",
                "alice.smith@example.com",
                "StrongP@ssword123",
                "StrongP@ssword123",
                "+1-555-0101",
                "Frontend Engineer",
                3.5,
                "Chicago, IL",
                "Passionate React developer"
        );

        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("verification email")))
                .andExpect(jsonPath("$.user.email").value("alice.smith@example.com"))
                .andExpect(jsonPath("$.user.emailVerified").value(false))
                .andExpect(jsonPath("$.user.verificationStatus").value("EMAIL_PENDING"));

        // Verify that raw token was dispatched via email and token hash was saved in DB
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).sendVerificationEmail(eq("alice.smith@example.com"), eq("Alice Smith"), tokenCaptor.capture());
        String rawToken = tokenCaptor.getValue();
        assertNotNull(rawToken);

        User savedUser = userRepository.findByEmail("alice.smith@example.com").orElseThrow();
        assertFalse(savedUser.isEmailVerified());
        assertNotNull(savedUser.getEmailVerificationTokenHash());
        assertEquals(64, savedUser.getEmailVerificationTokenHash().length()); // SHA-256 hex length
        assertNotEquals(rawToken, savedUser.getEmailVerificationTokenHash()); // Raw token is NOT stored in DB
    }

    @Test
    @DisplayName("Candidate registration fails when password and confirmation mismatch")
    void testCandidateRegistrationPasswordMismatchFails() throws Exception {
        CandidateRegisterRequest req = new CandidateRegisterRequest(
                "Bob Mismatch",
                "bob.mismatch@example.com",
                "Password123!",
                "DifferentPassword!",
                "+1-555-0102",
                "Developer",
                2.0,
                "Boston, MA",
                "Summary"
        );

        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Passwords do not match")));
    }

    @Test
    @DisplayName("Login is rejected for candidate who has not verified their email")
    void testCandidateLoginBlockedBeforeEmailVerification() throws Exception {
        CandidateRegisterRequest req = new CandidateRegisterRequest(
                "Unverified Candidate",
                "unverified.cand@example.com",
                "SecretPass123!",
                "SecretPass123!",
                "+1-555-0103",
                "Dev",
                1.0,
                "Remote",
                "Bio"
        );

        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        AuthRequest loginReq = new AuthRequest("unverified.cand@example.com", "SecretPass123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Email address has not been verified")));
    }

    @Test
    @DisplayName("Email verification with invalid token returns 400 Bad Request")
    void testEmailVerificationInvalidToken() throws Exception {
        VerifyEmailRequest verifyReq = new VerifyEmailRequest("totally-invalid-token-12345");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid or expired verification token")));
    }

    @Test
    @DisplayName("Email verification succeeds, marks verified, and enables login with HttpOnly cookie")
    void testCandidateVerificationAndLoginFlow() throws Exception {
        CandidateRegisterRequest req = new CandidateRegisterRequest(
                "Claire Verified",
                "claire.verified@example.com",
                "ValidSecret123!",
                "ValidSecret123!",
                "+1-555-0104",
                "QA Engineer",
                4.0,
                "Seattle, WA",
                "QA Expert"
        );

        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq("claire.verified@example.com"), eq("Claire Verified"), tokenCaptor.capture());
        String rawToken = tokenCaptor.getValue();

        // Verify email
        VerifyEmailRequest verifyReq = new VerifyEmailRequest(rawToken);
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        // Single-use verification token: second verification attempt fails
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid or expired verification token")));

        // Now login succeeds and sets HttpOnly talentiq_token cookie
        AuthRequest loginReq = new AuthRequest("claire.verified@example.com", "ValidSecret123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.emailVerified").value(true))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

        String setCookie = loginResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("talentiq_token="));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=Lax"));
    }

    @Test
    @DisplayName("Resend verification enforces rate limiting and avoids user enumeration")
    void testResendVerificationBehavior() throws Exception {
        CandidateRegisterRequest req = new CandidateRegisterRequest(
                "David Resend",
                "david.resend@example.com",
                "Password123!",
                "Password123!",
                "+1-555-0105",
                "Dev",
                1.0,
                "Remote",
                "Bio"
        );

        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Immediately requesting resend should hit the 2-minute rate limit
        ResendVerificationRequest resendReq = new ResendVerificationRequest("david.resend@example.com");
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resendReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Please wait 2 minutes")));

        // Resend for non-existent email returns generic message (anti-enumeration)
        ResendVerificationRequest ghostReq = new ResendVerificationRequest("ghost.user@nonexistent.domain");
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ghostReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("If an unverified account exists")));
    }

    @Test
    @DisplayName("Recruiter email verification sets status to REVIEW_PENDING; blocked from recruiter endpoints")
    void testRecruiterReviewPendingWorkflow() throws Exception {
        RecruiterRegisterRequest req = new RecruiterRegisterRequest(
                "Rachel Recruiter",
                "rachel@techcorp.com",
                "RecruiterSecret123!",
                "RecruiterSecret123!",
                "+1-555-0106",
                "TechCorp Inc.",
                "https://techcorp.com",
                "500 Innovation Way, Austin, TX",
                "Director of Talent Acquisition"
        );

        mockMvc.perform(post("/api/auth/register/recruiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.verificationStatus").value("EMAIL_PENDING"));

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq("rachel@techcorp.com"), eq("Rachel Recruiter"), tokenCaptor.capture());
        String rawToken = tokenCaptor.getValue();

        // Verify email
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailRequest(rawToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.verificationStatus").value("REVIEW_PENDING")); // Separate state from VERIFIED!

        // Recruiter can log in to view review status
        AuthRequest loginReq = new AuthRequest("rachel@techcorp.com", "RecruiterSecret123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.verificationStatus").value("REVIEW_PENDING"))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // But Recruiter-only endpoints remain blocked while REVIEW_PENDING
        mockMvc.perform(get("/api/dashboard/metrics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        JobRequest jobReq = new JobRequest("Unauthorized Job", "Engineering", "Remote", "FULL_TIME", 3, 5, List.of("Java"), "Desc", "ACTIVE");
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobReq)))
                .andExpect(status().isForbidden());

        // Recruiter cannot approve themselves (only ROLE_ADMIN can access admin endpoints)
        User user = userRepository.findByEmail("rachel@techcorp.com").orElseThrow();
        mockMvc.perform(post("/api/admin/recruiters/" + user.getId() + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Administrator can review, approve, and reject recruiters; approved recruiter can publish jobs")
    void testAdminApprovalAndRejectionWorkflow() throws Exception {
        User admin = testAuthHelper.createAdmin("platform.admin@talentiq.ai", "Platform Administrator");
        String adminToken = testAuthHelper.generateToken(admin);

        User pendingRecruiter = testAuthHelper.createPendingRecruiter("pending.approv@company.com", "Pending Approver");

        // Admin lists recruiters
        mockMvc.perform(get("/api/admin/recruiters?status=REVIEW_PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // Admin approves recruiter
        mockMvc.perform(post("/api/admin/recruiters/" + pendingRecruiter.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        // Now approved recruiter logs in and has full recruiter access
        AuthRequest loginReq = new AuthRequest("pending.approv@company.com", "PendingPass123!");
        MvcResult recruiterLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.verificationStatus").value("VERIFIED"))
                .andReturn();

        String approvedRecruiterToken = objectMapper.readTree(recruiterLogin.getResponse().getContentAsString()).get("token").asText();

        // Approved recruiter can successfully post a job
        JobRequest jobReq = new JobRequest(
                "Senior Cloud Architect",
                "Cloud Systems",
                "Remote",
                "FULL_TIME",
                5,
                10,
                List.of("AWS", "Kubernetes", "Java"),
                "High scale cloud architecture position.",
                "ACTIVE"
        );

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + approvedRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Senior Cloud Architect"));

        // Admin rejection flow
        User secondRecruiter = testAuthHelper.createPendingRecruiter("rejected.recruiter@shady.com", "Shady Recruiter");
        RecruiterReviewRequest rejectReq = new RecruiterReviewRequest(VerificationStatus.REJECTED, "Invalid business domain and incomplete tax registration");

        mockMvc.perform(post("/api/admin/recruiters/" + secondRecruiter.getId() + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Invalid business domain and incomplete tax registration"));
    }

    @Test
    @DisplayName("ROLE_CANDIDATE is forbidden from accessing recruiter endpoints")
    void testCandidateBlockedFromRecruiterEndpoints() throws Exception {
        User candidate = testAuthHelper.createVerifiedCandidate("cand.rbac@test.com", "Candidate RBAC");
        String candidateToken = testAuthHelper.generateToken(candidate);

        mockMvc.perform(get("/api/dashboard/metrics")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/candidates")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad Job\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Logout clears HttpOnly talentiq_token cookie and clears session")
    void testLogoutClearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}
