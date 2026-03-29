package com.hr.performancetracker.service;

import com.hr.performancetracker.dto.request.CreateReviewRequest;
import com.hr.performancetracker.dto.response.ReviewResponse;

import java.util.List;

/*
 * Review service contract
 * Controller depends on this interface
 * ReviewServiceImpl provides the actual implementation
 */
public interface ReviewService {

    ReviewResponse submitReview(CreateReviewRequest request);

    List<ReviewResponse> getEmployeeReviews(Long employeeId);
}