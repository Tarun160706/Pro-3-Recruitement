package com.talentiq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.entity.*;
import com.talentiq.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public DataInitializer(UserRepository userRepository,
                           JobRepository jobRepository,
                           CandidateRepository candidateRepository,
                           InterviewSessionRepository sessionRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${talentiq.admin.email:admin@talentiq.ai}") String adminEmail,
                           @Value("${talentiq.admin.password:Admin#2026!}") String adminPassword,
                           @Value("${talentiq.admin.name:TalentIQ Administrator}") String adminName) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail != null ? adminEmail.toLowerCase().trim() : "admin@talentiq.ai";
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting TalentIQ initialization and demo account sanitization...");

        // 1. Ensure primary administrator account exists
        User adminUser = userRepository.findByEmail(adminEmail).orElseGet(() -> {
            log.info("Provisioning initial TalentIQ platform administrator: {}", adminEmail);
            User admin = new User(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    adminName,
                    Role.ROLE_ADMIN,
                    "+1-555-ADMIN"
            );
            admin.setEmailVerified(true);
            admin.setVerificationStatus(VerificationStatus.VERIFIED);
            return userRepository.save(admin);
        });

        // Ensure admin user is verified and has admin role
        if (adminUser.getRole() != Role.ROLE_ADMIN || !adminUser.isEmailVerified()) {
            adminUser.setRole(Role.ROLE_ADMIN);
            adminUser.setEmailVerified(true);
            adminUser.setVerificationStatus(VerificationStatus.VERIFIED);
            adminUser = userRepository.save(adminUser);
        }

        final User finalAdmin = adminUser;

        // 2. Safe cleanup of legacy pre-seeded demo accounts (recruiter@talentiq.ai & candidate@talentiq.ai)
        // Reassign any referenced jobs/sessions to admin to preserve data integrity before deleting demo users.
        userRepository.findByEmail("recruiter@talentiq.ai").ifPresent(demoRecruiter -> {
            log.info("Sanitizing legacy demo recruiter account: {}", demoRecruiter.getEmail());

            // Reassign jobs linked to demo recruiter to admin
            List<Job> linkedJobs = jobRepository.findByRecruiterId(demoRecruiter.getId());
            for (Job job : linkedJobs) {
                job.setRecruiter(finalAdmin);
                jobRepository.save(job);
            }

            // Reassign interview sessions
            List<InterviewSession> linkedSessions = sessionRepository.findAll().stream()
                    .filter(s -> s.getRecruiter() != null && s.getRecruiter().getId().equals(demoRecruiter.getId()))
                    .toList();
            for (InterviewSession s : linkedSessions) {
                s.setRecruiter(finalAdmin);
                sessionRepository.save(s);
            }

            userRepository.delete(demoRecruiter);
            log.info("Demo recruiter account removed successfully.");
        });

        userRepository.findByEmail("candidate@talentiq.ai").ifPresent(demoCandidate -> {
            log.info("Sanitizing legacy demo candidate account: {}", demoCandidate.getEmail());
            candidateRepository.findByEmail("candidate@talentiq.ai").ifPresent(c -> {
                c.setUser(null);
                candidateRepository.save(c);
            });
            userRepository.delete(demoCandidate);
            log.info("Demo candidate account removed successfully.");
        });

        log.info("TalentIQ initialization complete. Demo accounts purged; platform administrator active.");
    }
}
