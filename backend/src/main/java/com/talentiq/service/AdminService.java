package com.talentiq.service;

import com.talentiq.dto.RecruiterReviewRequest;
import com.talentiq.dto.UserResponse;
import com.talentiq.entity.Role;
import com.talentiq.entity.User;
import com.talentiq.entity.VerificationStatus;
import com.talentiq.exception.BadRequestException;
import com.talentiq.exception.ResourceNotFoundException;
import com.talentiq.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public AdminService(UserRepository userRepository, ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getRecruiters(VerificationStatus status) {
        List<User> recruiters;
        if (status != null) {
            recruiters = userRepository.findByRoleAndVerificationStatus(Role.ROLE_RECRUITER, status);
        } else {
            recruiters = userRepository.findByRole(Role.ROLE_RECRUITER);
        }
        return recruiters.stream().map(UserResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getRecruiterById(Long id) {
        User recruiter = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + id));

        if (recruiter.getRole() != Role.ROLE_RECRUITER) {
            throw new BadRequestException("User with id " + id + " is not a recruiter");
        }

        return new UserResponse(recruiter);
    }

    @Transactional
    public UserResponse approveRecruiter(Long id) {
        User recruiter = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + id));

        if (recruiter.getRole() != Role.ROLE_RECRUITER) {
            throw new BadRequestException("User with id " + id + " is not a recruiter");
        }

        recruiter.setVerificationStatus(VerificationStatus.VERIFIED);
        recruiter.setRejectionReason(null);
        User saved = userRepository.save(recruiter);

        activityLogService.log(
                "RECRUITER_APPROVED",
                "Recruiter Application Approved",
                "Recruiter " + saved.getFullName() + " (" + saved.getEmail() + ") approved by administrator"
        );

        return new UserResponse(saved);
    }

    @Transactional
    public UserResponse rejectRecruiter(Long id, RecruiterReviewRequest request) {
        User recruiter = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + id));

        if (recruiter.getRole() != Role.ROLE_RECRUITER) {
            throw new BadRequestException("User with id " + id + " is not a recruiter");
        }

        String reason = (request != null && request.getRejectionReason() != null && !request.getRejectionReason().isBlank())
                ? request.getRejectionReason().trim()
                : "Recruiter company verification did not satisfy platform compliance criteria.";

        recruiter.setVerificationStatus(VerificationStatus.REJECTED);
        recruiter.setRejectionReason(reason);
        User saved = userRepository.save(recruiter);

        activityLogService.log(
                "RECRUITER_REJECTED",
                "Recruiter Application Rejected",
                "Recruiter " + saved.getFullName() + " (" + saved.getEmail() + ") rejected by administrator. Reason: " + reason
        );

        return new UserResponse(saved);
    }
}

