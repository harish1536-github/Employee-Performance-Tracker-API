package com.hr.performancetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "goals",
        indexes = {
                @Index(
                        name       = "idx_goals_cycle_id",
                        columnList = "cycle_id"
                ),
                @Index(
                        name       = "idx_goals_employee_id",
                        columnList = "employee_id"
                ),
                /*
                 * Composite index on cycle_id + status
                 * Used by GET /cycles/{id}/summary
                 * When counting goals by status for a cycle
                 * PostgreSQL scans this index instead of full table
                 */
                @Index(
                        name       = "idx_goals_cycle_status",
                        columnList = "cycle_id, status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal implements Serializable {

    /*
     * GoalStatus defined inside Goal class
     * It belongs to Goal so it lives here
     *
     * EnumType.STRING stores "PENDING" "COMPLETED" "MISSED"
     * NOT 0, 1, 2 (EnumType.ORDINAL)
     * ORDINAL breaks if you insert a new value in the middle
     * STRING is always safe
     */
    public enum GoalStatus {
        PENDING, COMPLETED, MISSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle cycle;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = GoalStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}