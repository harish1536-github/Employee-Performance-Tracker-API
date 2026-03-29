package com.hr.performancetracker.controller;

import com.hr.performancetracker.dto.request.CreateReviewRequest;
import com.hr.performancetracker.dto.response.ReviewResponse;
import com.hr.performancetracker.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * No class level @RequestMapping here
 *
 * WHY:
 * This controller has two different base paths:
 * POST /reviews                  no employee prefix
 * GET  /employees/{id}/reviews   under employees prefix
 *
 * If we put @RequestMapping("/reviews") the GET would not work
 * If we put @RequestMapping("/employees") the POST would not work
 *
 * Solution: no class level mapping
 * Each method defines its full path independently
 *
 * No Swagger annotations anywhere in this controller
 */
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /*
     * POST /reviews
     * Submits a performance review
     *
     * Multiple reviews per employee per cycle are allowed
     * Supports mid-cycle check-ins and multiple reviewers
     *
     * Returns 201 Created with saved review including cycle details
     * Returns 400 if validation fails
     * Returns 404 if employee or cycle not found
     */
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> submit(
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response =
                reviewService.submitReview(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * GET /employees/{id}/reviews
     * Returns all reviews for a specific employee
     * Includes cycle details so client does not need
     * a second API call to get cycle info
     *
     * Ordered by submittedAt DESC most recent first
     *
     * Results cached in Redis for 2 minutes
     * Cache evicted when new review submitted for this employee
     *
     * Returns 200 OK with list can be empty
     * Returns 404 if employee does not exist
     *
     * WHY /employees/{id}/reviews and not /reviews?employeeId=x:
     * RESTful resource nesting
     * Reads as reviews belonging to employee id
     * Cleaner and more standard REST design
     */
    @GetMapping("/employees/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getEmployeeReviews(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                reviewService.getEmployeeReviews(id));
    }
}