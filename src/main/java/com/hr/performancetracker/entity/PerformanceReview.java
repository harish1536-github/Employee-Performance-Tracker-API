package com.hr.performancetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * @NamedEntityGraph here solves N+1 problem
 *
 * Without EntityGraph:
 * Load 10 reviews      = 1 query
 * Load employee each   = 10 queries
 * Load cycle each      = 10 queries
 * Total               = 21 queries
 *
 * With EntityGraph:
 * One JOIN query loads reviews + employees + cycles
 * Total               = 1 query
 */
@Entity
@Table(
        name = "performance_reviews",
        indexes = {
                @Index(
                        name       = "idx_reviews_employee_id",
                        columnList = "employee_id"
                ),
                @Index(
                        name       = "idx_reviews_cycle_id",
                        columnList = "cycle_id"
                ),
                @Index(
                        name       = "idx_reviews_employee_cycle",
                        columnList = "employee_id, cycle_id"
                )
        }
)
@NamedEntityGraph(
        name           = "PerformanceReview.withEmployeeAndCycle",
        attributeNodes = {
                @NamedAttributeNode("employee"),
                @NamedAttributeNode("cycle")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * FetchType.LAZY = do not load Employee
     * when loading this Review
     *
     * optional = false = employee_id cannot be null
     * Matches DB NOT NULL constraint
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle cycle;

    /*
     * Rating enforced at both levels:
     * Java: @Min @Max in request DTO
     * DB:   CHECK constraint in SQL migration
     */
    @Column(nullable = false)
    private Integer rating;

    /*
     * TEXT has no length limit
     * Reviewer notes can be long paragraphs
     * VARCHAR(255) would be too short
     */
    @Column(columnDefinition = "TEXT")
    private String reviewerNotes;

    @Column(nullable = false, length = 100)
    private String reviewerName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}