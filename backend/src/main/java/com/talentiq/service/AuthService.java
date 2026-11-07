package com.talentiq.service;

import com.talentiq.config.CustomUserDetails;
import com.talentiq.config.JwtAuthenticationFilter;
import com.talentiq.config.JwtTokenProvider;
import com.talentiq.dto.*;
import com.talentiq.entity.Candidate;
import com.talentiq.entity.Role;
import com.talentiq.entity.User;
import com.talentiq.entity.VerificationStatus;
import com.talentiq.exception.BadRequestException;
import com.talentiq.exception.UnauthorizedException;
import com.talentiq.repository.CandidateRepository;
import com.talentiq.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final boolean cookieSecure;

    public AuthService(UserRepository userRepository,
                       CandidateRepository candidateRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       ActivityLogService activityLogService,
                       EmailService emailService,
                       TokenService tokenService,
                       @Value("${talentiq.cookie.secure:false}") boolean cookieSecure) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.activityLogService = activityLogService;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.cookieSecure = cookieSecure;
    }

    @Transactional
    public AuthResponse registerCandidate(CandidateRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists");
        }

        String email = request.getEmail().toLowerCase().trim();
        String rawToken = tokenService.generateSecureToken();
        String tokenHash = tokenService.hashToken(rawToken);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setRole(Role.ROLE_CANDIDATE);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setEmailVerified(false);
        user.setVerificationStatus(VerificationStatus.EMAIL_PENDING);
        user.setEmailVerificationTokenHash(tokenHash);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setLastVerificationEmailSentAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Seed / initialize candidate profile
        Candidate candidate = new Candidate(
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                request.getCurrentTitle() != null ? request.getCurrentTitle().trim() : "Candidate",
                request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0.0,
                request.getLocation() != null ? request.getLocation().trim() : "Remote",
                request.getSummary() != null ? request.getSummary().trim() : "TalentIQ Candidate Profile"
        );
        candidate.setUser(savedUser);
        candidateRepository.save(candidate);

        // Send real email verification link (fails safely with configuration exception if SMTP is missing)
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), rawToken);

        activityLogService.log("USER_REGISTERED", "Candidate Registered",
                "New candidate registered: " + savedUser.getFullName());

        return new AuthResponse(
                null,
                new UserResponse(savedUser),
                "Registration successful. A verification email has been sent to your inbox. Please verify your email before logging in."
        );
    }

    @Transactional
    public AuthResponse registerRecruiter(RecruiterRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getWorkEmail().toLowerCase().trim())) {
            throw new BadRequestException("An account with email " + request.getWorkEmail() + " already exists");
        }

        String email = request.getWorkEmail().toLowerCase().trim();
        String rawToken = tokenService.generateSecureToken();
        String tokenHash = tokenService.hashToken(rawToken);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setRole(Role.ROLE_RECRUITER);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setEmailVerified(false);
        user.setVerificationStatus(VerificationStatus.EMAIL_PENDING);
        user.setCompanyName(request.getCompanyName().trim());
        user.setCompanyWebsite(request.getCompanyWebsite().trim());
        user.setCompanyAddress(request.getCompanyAddress().trim());
        user.setDesignation(request.getDesignation().trim());
        user.setEmailVerificationTokenHash(tokenHash);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setLastVerificationEmailSentAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Send real email verification link
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), rawToken);

        activityLogService.log("USER_REGISTERED", "Recruiter Registered",
                "New recruiter registered: " + savedUser.getFullName() + " for " + savedUser.getCompanyName());

        return new AuthResponse(
                null,
                new UserResponse(savedUser),
                "Registration successful. A verification email has been sent to your work email. Please verify your email to submit your company profile for review."
        );
    }

    @Transactional
    public UserResponse verifyEmail(VerifyEmailRequest request) {
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            throw new BadRequestException("Verification token cannot be blank");
        }

        String tokenHash = tokenService.hashToken(request.getToken().trim());
        User user = userRepository.findByEmailVerificationTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("This account email has already been verified");
        }

        if (user.getEmailVerificationTokenExpiry() != null &&
                user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired. Please request a new verification link.");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationTokenHash(null);
        user.setEmailVerificationTokenExpiry(null);

        // Candidate accounts are immediately VERIFIED upon email verification.
        // Recruiter accounts transition to REVIEW_PENDING awaiting administrative review.
        if (user.getRole() == Role.ROLE_RECRUITER) {
            user.setVerificationStatus(VerificationStatus.REVIEW_PENDING);
        } else {
            user.setVerificationStatus(VerificationStatus.VERIFIED);
        }

        User savedUser = userRepository.save(user);

        activityLogService.log("EMAIL_VERIFIED", "Email Address Verified",
                "Email successfully verified for account: " + savedUser.getEmail());

        return new UserResponse(savedUser);
    }

    @Transactional
    public AuthResponse resendVerification(ResendVerificationRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);

        // Prevent email enumeration: return standard response even if email is not found or already verified
        if (userOpt.isEmpty() || userOpt.get().isEmailVerified()) {
            return new AuthResponse("If an unverified account exists with this email address, a new verification link has been sent.");
        }

        User user = userOpt.get();

        // Enforce rate limiting: 2 minutes cooldown between verification emails
        if (user.getLastVerificationEmailSentAt() != null &&
                user.getLastVerificationEmailSentAt().plusMinutes(2).isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Please wait 2 minutes before requesting another verification email");
        }

        String rawToken = tokenService.generateSecureToken();
        String tokenHash = tokenService.hashToken(rawToken);

        user.setEmailVerificationTokenHash(tokenHash);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setLastVerificationEmailSentAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), rawToken);

        return new AuthResponse("If an unverified account exists with this email address, a new verification link has been sent.");
    }

    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        String email = request.getEmail().toLowerCase().trim();

        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Reject login for unverified accounts
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email address has not been verified. Please check your inbox or request a new verification link.");
        }

        String token = tokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                user.getFullName(),
                user.getRole().name()
        );

        // Attach HttpOnly cookie
        if (response != null) {
            ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, token)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(86400) // 24 hours
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return new AuthResponse(token, new UserResponse(user), "Login successful");
    }

    public AuthResponse logout(HttpServletResponse response) {
        if (response != null) {
            ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, "")
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        SecurityContextHolder.clearContext();
        return new AuthResponse("Logged out successfully");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Legacy fallback registration method
        if (request.getRole() == Role.ROLE_RECRUITER) {
            RecruiterRegisterRequest req = new RecruiterRegisterRequest(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getPassword(),
                    request.getPhone(),
                    "TalentIQ Partner Company",
                    "https://company.example.com",
                    "Corporate Headquarters",
                    "Technical Recruiter"
            );
            return registerRecruiter(req);
        } else {
            CandidateRegisterRequest req = new CandidateRegisterRequest(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getPassword(),
                    request.getPhone(),
                    "Software Engineer",
                    1.0,
                    "Remote",
                    "Candidate Profile"
            );
            return registerCandidate(req);
        }
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public Optional<User> getOptionalAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return Optional.empty();
        }
        return userRepository.findById(userDetails.getId());
    }
}
