package com.talentiq.repository;

import com.talentiq.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);
    Optional<Candidate> findByUserId(Long userId);

    @Query("SELECT c FROM Candidate c WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(c.currentTitle) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(:minExp IS NULL OR c.yearsOfExperience >= :minExp)")
    List<Candidate> searchCandidates(@Param("search") String search, @Param("minExp") Double minExp);
}
