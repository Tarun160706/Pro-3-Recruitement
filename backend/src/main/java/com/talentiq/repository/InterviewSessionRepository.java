package com.talentiq.repository;

import com.talentiq.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByJobId(Long jobId);
    List<InterviewSession> findByCandidateId(Long candidateId);
    List<InterviewSession> findByRecruiterId(Long recruiterId);
    long countByStatus(String status);

    @Query("SELECT s FROM InterviewSession s WHERE s.candidate.user.id = :userId")
    List<InterviewSession> findByCandidateUserId(@Param("userId") Long userId);
}
