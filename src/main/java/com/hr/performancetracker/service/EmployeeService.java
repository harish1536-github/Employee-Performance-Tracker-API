package com.hr.performancetracker.service;

import com.hr.performancetracker.dto.request.CreateEmployeeRequest;
import com.hr.performancetracker.dto.response.EmployeeResponse;

import java.util.List;

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