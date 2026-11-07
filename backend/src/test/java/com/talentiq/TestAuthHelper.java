package com.talentiq;

import com.talentiq.config.JwtTokenProvider;
import com.talentiq.entity.Candidate;
import com.talentiq.entity.Role;
import com.talentiq.entity.User;
import com.talentiq.entity.VerificationStatus;
import com.talentiq.repository.CandidateRepository;
import com.talentiq.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestAuthHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public User createVerifiedRecruiter(String email, String fullName) {
        return userRepository.findByEmail(email).map(existing -> {
            existing.setEmailVerified(true);
            existing.setVerificationStatus(VerificationStatus.VERIFIED);
            existing.setPassword(passwordEncoder.encode("RecruiterPass123!"));
            return userRepository.save(existing);
        }).orElseGet(() -> {
            User recruiter = new User();
            recruiter.setEmail(email);
            recruiter.setPassword(passwordEncoder.encode("RecruiterPass123!"));
            recruiter.setFullName(fullName);
            recruiter.setRole(Role.ROLE_RECRUITER);
            recruiter.setPhone("+1-555-0199");
            recruiter.setCompanyName("Acme Talent Corp");
            recruiter.setCompanyWebsite("https://acme.example.com");
            recruiter.setCompanyAddress("123 Tech Blvd, Silicon Valley");
            recruiter.setDesignation("Lead Talent Partner");
            recruiter.setEmailVerified(true);
            recruiter.setVerificationStatus(VerificationStatus.VERIFIED);
            return userRepository.save(recruiter);
        });
    }

    public User createVerifiedCandidate(String email, String fullName) {
        User user = userRepository.findByEmail(email).map(existing -> {
            existing.setEmailVerified(true);
            existing.setVerificationStatus(VerificationStatus.VERIFIED);
            existing.setPassword(passwordEncoder.encode("CandidatePass123!"));
            return userRepository.save(existing);
        }).orElseGet(() -> {
            User candidateUser = new User();
            candidateUser.setEmail(email);
            candidateUser.setPassword(passwordEncoder.encode("CandidatePass123!"));
            candidateUser.setFullName(fullName);
            candidateUser.setRole(Role.ROLE_CANDIDATE);
            candidateUser.setPhone("+1-555-0144");
            candidateUser.setEmailVerified(true);
            candidateUser.setVerificationStatus(VerificationStatus.VERIFIED);
            return userRepository.save(candidateUser);
        });

        if (candidateRepository.findByEmail(email).isEmpty()) {
            Candidate candidate = new Candidate(fullName, email, "+1-555-0144", "Senior Engineer", 5.0, "San Francisco, CA", "Talented engineer");
            candidate.setUser(user);
            candidateRepository.save(candidate);
        }

        return user;
    }

    public User createPendingRecruiter(String email, String fullName) {
        return userRepository.findByEmail(email).map(existing -> {
            existing.setEmailVerified(true);
            existing.setVerificationStatus(VerificationStatus.REVIEW_PENDING);
            existing.setPassword(passwordEncoder.encode("PendingPass123!"));
            return userRepository.save(existing);
        }).orElseGet(() -> {
            User recruiter = new User();
            recruiter.setEmail(email);
            recruiter.setPassword(passwordEncoder.encode("PendingPass123!"));
            recruiter.setFullName(fullName);
            recruiter.setRole(Role.ROLE_RECRUITER);
            recruiter.setPhone("+1-555-0155");
            recruiter.setCompanyName("Pending Venture");
            recruiter.setCompanyWebsite("https://pending.example.com");
            recruiter.setCompanyAddress("456 Review Way");
            recruiter.setDesignation("Recruitment Specialist");
            recruiter.setEmailVerified(true);
            recruiter.setVerificationStatus(VerificationStatus.REVIEW_PENDING);
            return userRepository.save(recruiter);
        });
    }

    public User createAdmin(String email, String fullName) {
        return userRepository.findByEmail(email).map(existing -> {
            existing.setEmailVerified(true);
            existing.setVerificationStatus(VerificationStatus.VERIFIED);
            existing.setRole(Role.ROLE_ADMIN);
            return userRepository.save(existing);
        }).orElseGet(() -> {
            User admin = new User();
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode("AdminPass123!"));
            admin.setFullName(fullName);
            admin.setRole(Role.ROLE_ADMIN);
            admin.setEmailVerified(true);
            admin.setVerificationStatus(VerificationStatus.VERIFIED);
            return userRepository.save(admin);
        });
    }

    public String generateToken(User user) {
        return tokenProvider.generateToken(user.getEmail(), user.getId(), user.getFullName(), user.getRole().name());
    }
}

