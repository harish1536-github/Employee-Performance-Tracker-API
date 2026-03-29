package com.hr.performancetracker.repository;

import com.hr.performancetracker.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    /*
     * Spring Data generates this SQL automatically:
     * SELECT * FROM employees
     * WHERE name = ? AND department = ?
     *
     * Used to check duplicates before creating an employee
     */
    boolean existsByNameAndDepartment(
            String name, String department);

    /*
     * Custom JPQL query needed here because:
     *
     * 1. We need AVG(rating) per employee
     *    which requires GROUP BY
     *
     * 2. We filter by that average
     *    which requires HAVING not WHERE
     *    HAVING filters after grouping
     *    WHERE filters before grouping
     *
     * 3. LEFT JOIN includes employees with zero reviews
     *    COALESCE converts null avg to 0
     *
     * LOWER() on both sides makes search case insensitive
     * department=engineering matches Engineering
     *
     * JPQL uses entity names not table names:
     * Employee not employees
     * PerformanceReview not performance_reviews
     * joiningDate not joining_date
     */
    @Query("""
            SELECT e FROM Employee e
            LEFT JOIN PerformanceReview r
                ON r.employee = e
            WHERE LOWER(e.department) = LOWER(:department)
            GROUP BY e
            HAVING COALESCE(AVG(r.rating), 0) >= :minRating
            ORDER BY e.name ASC
            """)
    List<Employee> findByDepartmentAndMinRating(
            @Param("department") String department,
            @Param("minRating") Double minRating
    );
}