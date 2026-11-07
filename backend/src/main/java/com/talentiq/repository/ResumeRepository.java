package com.talentiq.repository;

import com.talentiq.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByCandidateId(Long candidateId);
    Optional<Resume> findFirstByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}
