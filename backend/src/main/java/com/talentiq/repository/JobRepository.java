package com.talentiq.repository;

import com.talentiq.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(String status);
    List<Job> findByRecruiterId(Long recruiterId);
    long countByStatus(String status);

    @Query("SELECT j FROM Job j WHERE " +
           "(CAST(:status AS string) IS NULL OR j.status = CAST(:status AS string)) AND " +
           "(CAST(:department AS string) IS NULL OR LOWER(j.department) = LOWER(CAST(:department AS string))) AND " +
           "(CAST(:search AS string) IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    List<Job> searchJobs(@Param("status") String status,
                         @Param("department") String department,
                         @Param("search") String search);
}
