package com.hr.performancetracker.exception;

/*
 * Thrown when a business rule is violated
 *
 * Examples:
 * Duplicate employee in same department
 * Duplicate cycle name
 * End date before start date
 *
 * GlobalExceptionHandler converts this to HTTP 400
 *
 * Separate from ResourceNotFoundException because:
 * Different HTTP status 400 vs 404
 * Different meaning bad input vs not found
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public static BusinessException duplicateEmployee(
            String name, String department) {
        return new BusinessException(
                String.format(
                        "Employee '%s' already exists in " +
                                "department '%s'",
                        name, department)
        );
    }

    public static BusinessException duplicateCycle(String name) {
        return new BusinessException(
                String.format(
                        "Review cycle with name '%s' already exists",
                        name)
        );
    }

    public static BusinessException invalidCycleDates() {
        return new BusinessException(
                "End date must be after start date"
        );
    }
}