package com.hr.performancetracker.repository;

import com.hr.performancetracker.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository
        extends JpaRepository<Goal, Long> {

    /*
     * Single query for all goal counts
     *
     * WHY one query instead of three:
     *
     * BAD approach needs 3 queries:
     * countByCycleIdAndStatus(cycleId, COMPLETED)
     * countByCycleIdAndStatus(cycleId, MISSED)
     * countByCycleIdAndStatus(cycleId, PENDING)
     *
     * GOOD approach this method uses 1 query:
     * Uses conditional SUM inside one SQL statement
     * PostgreSQL scans goals table once
     * Uses idx_goals_cycle_status composite index
     *
     * Object[] layout:
     * [0] = count of COMPLETED (Long)
     * [1] = count of MISSED    (Long)
     * [2] = count of PENDING   (Long)
     * [3] = total count        (Long)
     *
     * SUM returns null if no rows match
     * We handle null in service layer
     */
    @Query("""
            SELECT
                SUM(CASE WHEN g.status = 'COMPLETED'
                    THEN 1 ELSE 0 END),
                SUM(CASE WHEN g.status = 'MISSED'
                    THEN 1 ELSE 0 END),
                SUM(CASE WHEN g.status = 'PENDING'
                    THEN 1 ELSE 0 END),
                COUNT(g)
            FROM Goal g
            WHERE g.cycle.id = :cycleId
            """)
    List<Object[]> getGoalCountsByCycleId(@Param("cycleId") Long cycleId);
}