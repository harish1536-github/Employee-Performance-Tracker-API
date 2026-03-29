package com.hr.performancetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Serializable is required because this entity
 * gets stored in Redis cache
 * Without it Redis cannot serialize the object
 *
 * @NamedEntityGraph tells JPA:
 * When I ask for Employee.withReviews,
 * fetch reviews in the same query using JOIN
 * This prevents N+1 queries
 */
@Entity
@Table(
        name = "employees",
        indexes = {
                @Index(
                        name         = "idx_employees_department",
                        columnList   = "department"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name        = "uq_employee_name_dept",
                        columnNames = {"name", "department"}
                )
        }
)
@NamedEntityGraph(
        name           = "Employee.withReviews",
        attributeNodes = @NamedAttributeNode("reviews")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 100)
    private String role;

    /*
     * LocalDate maps to DATE column in PostgreSQL
     * Stores only the date without time
     */
    @Column(nullable = false)
    private LocalDate joiningDate;

    /*
     * updatable = false means this column is set once
     * and never updated even if save() is called again
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /*
     * mappedBy = "employee" means the foreign key
     * lives in PerformanceReview table not here
     *
     * FetchType.LAZY means do NOT load reviews
     * when loading Employee
     * Only load when explicitly accessed
     *
     * CascadeType.ALL means if Employee deleted
     * all their reviews are deleted too
     * Matches ON DELETE CASCADE in SQL
     *
     * orphanRemoval = true means if a review is
     * removed from this list delete it from DB
     */
    @OneToMany(
            mappedBy      = "employee",
            fetch         = FetchType.LAZY,
            cascade       = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<PerformanceReview> reviews = new ArrayList<>();

    @OneToMany(
            mappedBy      = "employee",
            fetch         = FetchType.LAZY,
            cascade       = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Goal> goals = new ArrayList<>();

    /*
     * @PrePersist runs before first save to database
     * @PreUpdate runs before every update
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}