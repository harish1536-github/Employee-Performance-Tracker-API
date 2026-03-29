package com.hr.performancetracker.exception;

/*
 * Thrown when a resource does not exist in database
 * GlobalExceptionHandler converts this to HTTP 404
 *
 * Extends RuntimeException so callers do not need
 * to catch it explicitly
 * GlobalExceptionHandler catches it centrally
 */
public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException employee(Long id) {
        return new ResourceNotFoundException(
                "Employee not found with id: " + id
        );
    }

    public static ResourceNotFoundException cycle(Long id) {
        return new ResourceNotFoundException(
                "Review cycle not found with id: " + id
        );
    }

    public static ResourceNotFoundException review(Long id) {
        return new ResourceNotFoundException(
                "Performance review not found with id: " + id
        );
    }
}