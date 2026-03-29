package com.hr.performancetracker.repository;

import com.hr.performancetracker.entity.ReviewCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCycleRepository
        extends JpaRepository<ReviewCycle, Long> {

    /*
     * Spring Data generates:
     * SELECT COUNT(*) > 0 FROM review_cycles WHERE name = ?
     * Used to check for duplicate cycle names
     */
    boolean existsByName(String name);

    /*
     * Spring Data generates:
     * SELECT * FROM review_cycles WHERE name = ?
     */
    Optional<ReviewCycle> findByName(String name);
}