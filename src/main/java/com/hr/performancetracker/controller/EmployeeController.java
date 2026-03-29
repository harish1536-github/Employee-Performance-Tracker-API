package com.hr.performancetracker.controller;


import com.hr.performancetracker.dto.request.CreateEmployeeRequest;
import com.hr.performancetracker.dto.response.EmployeeResponse;
import com.hr.performancetracker.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * No Swagger annotations here
 * Clean controller with only Spring annotations
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    /*
     * POST /employees
     * Creates a new employee
     * Returns 201 Created
     */
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(
            @Valid @RequestBody CreateEmployeeRequest request) {

        EmployeeResponse response =
                employeeService.createEmployee(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * GET /employees/{id}
     * Get employee by ID
     * Returns 200 OK or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(employeeService.getById(id));
    }

    /*
     * GET /employees?department=Engineering&minRating=4.0
     * Filter employees by department and minimum average rating
     * Returns 200 OK with list (can be empty)
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> filter(
            @RequestParam String department,

            @RequestParam(defaultValue = "0")
            @DecimalMin(
                    value   = "0",
                    message = "Minimum rating cannot be negative")
            Double minRating) {

        return ResponseEntity.ok(
                employeeService.filter(department, minRating));
    }
}