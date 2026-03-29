package com.hr.performancetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "review_cycles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name        = "uq_cycle_name",
                        columnNames = {"name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCycle implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /*
     * No CascadeType.ALL here intentionally
     * In real HR systems you may want to archive a cycle
     * without losing review history
     * Different from Employee which cascades deletes
     */
    @OneToMany(
            mappedBy = "cycle",
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<PerformanceReview> reviews = new ArrayList<>();

    @OneToMany(
            mappedBy = "cycle",
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<Goal> goals = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}