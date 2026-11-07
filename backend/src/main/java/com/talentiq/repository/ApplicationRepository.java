package com.talentiq.repository;

import com.talentiq.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJobId(Long jobId);
    List<Application> findByCandidateId(Long candidateId);
    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);
    long countByStatus(String status);

    @Query("SELECT a FROM Application a WHERE a.candidate.user.id = :userId")
    List<Application> findByCandidateUserId(@Param("userId") Long userId);

    @Query("SELECT a.stage as stage, COUNT(a) as count FROM Application a GROUP BY a.stage")
    List<Object[]> countApplicationsByStage();
}
