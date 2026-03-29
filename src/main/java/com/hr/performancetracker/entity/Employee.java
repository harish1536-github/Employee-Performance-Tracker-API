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