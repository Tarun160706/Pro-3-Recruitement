package com.talentiq.repository;

import com.talentiq.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {
    List<JobMatch> findByJobId(Long jobId);
    List<JobMatch> findByCandidateId(Long candidateId);
    Optional<JobMatch> findFirstByJobIdAndCandidateIdOrderByCreatedAtDesc(Long jobId, Long candidateId);
}
