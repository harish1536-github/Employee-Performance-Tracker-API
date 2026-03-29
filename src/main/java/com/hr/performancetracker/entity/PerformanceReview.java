package com.hr.performancetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;


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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle cycle;

    @Column(nullable = false)
    private Integer rating;

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