package com.hr.performancetracker.service;

import com.hr.performancetracker.dto.request.CreateEmployeeRequest;
import com.hr.performancetracker.dto.response.EmployeeResponse;

import java.util.List;

/*
 * WHY AN INTERFACE HERE?
 *
 * This is a CONTRACT
 * It says: "Anyone who implements me MUST provide these methods"
 *
 * Controller only knows about this interface
 * Controller does NOT know about EmployeeServiceImpl
 * This is the Dependency Inversion Principle (D in SOLID)
 *
 * Benefits:
 * 1. Controller stays clean - just calls methods, does not care how
 * 2. Implementation can change without touching controller
 * 3. In tests, you can mock this interface easily
 * 4. Multiple implementations possible (e.g. v1, v2)
 */
public interface EmployeeService {

    /*
     * Each method here is a CONTRACT
     * No body, no logic, just the signature
     * EmployeeServiceImpl provides the actual logic
     */

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse getById(Long id);

    List<EmployeeResponse> filter(String department, Double minRating);
}