package com.hr.performancetracker.repository;

import com.hr.performancetracker.entity.PerformanceReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceReviewRepository
        extends JpaRepository<PerformanceReview, Long> {

    /*
     * @EntityGraph is the Spring Data JPA way to solve N+1
     *
     * Uses NamedEntityGraph defined on PerformanceReview entity
     * Generates a single LEFT JOIN query that loads:
     * reviews + employee + cycle all at once
     *
     * OrderBySubmittedAtDesc is read from method name by Spring:
     * ORDER BY submitted_at DESC
     * Most recent reviews come first
     */
    @EntityGraph(value = "PerformanceReview.withEmployeeAndCycle")
    List<PerformanceReview> findByEmployeeIdOrderBySubmittedAtDesc(
            Long employeeId);

    /*
     * AVG of all ratings for a specific cycle
     * Returns null if no reviews exist yet
     * We handle null in service layer
     */
    @Query("""
            SELECT AVG(r.rating)
            FROM PerformanceReview r
            WHERE r.cycle.id = :cycleId
            """)
    Double findAverageRatingByCycleId(
            @Param("cycleId") Long cycleId);

    /*
     * Finds top performer in a cycle
     * Returns Object[] because we select specific columns
     * not a full entity
     *
     * Object[] layout:
     * [0] = employee id     (Long)
     * [1] = employee name   (String)
     * [2] = department      (String)
     * [3] = avg rating      (Double)
     *
     * LIMIT 1 = only the single top performer
     * ORDER BY AVG DESC = highest rated first
     */
    @Query("""
            SELECT
                r.employee.id,
                r.employee.name,
                r.employee.department,
                AVG(r.rating) AS avgRating
            FROM PerformanceReview r
            WHERE r.cycle.id = :cycleId
            GROUP BY
                r.employee.id,
                r.employee.name,
                r.employee.department
            ORDER BY avgRating DESC
            LIMIT 1
            """)
    List<Object[]> findTopPerformerByCycleId(
            @Param("cycleId") Long cycleId);

    /*
     * Spring Data generates:
     * SELECT COUNT(*) FROM performance_reviews
     * WHERE cycle_id = ?
     */
    Long countByCycleId(Long cycleId);
}