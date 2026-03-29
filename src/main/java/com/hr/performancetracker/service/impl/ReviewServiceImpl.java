package com.hr.performancetracker.service.impl;

import com.hr.performancetracker.config.CacheConfig;
import com.hr.performancetracker.dto.request.CreateReviewRequest;
import com.hr.performancetracker.dto.response.ReviewResponse;
import com.hr.performancetracker.entity.Employee;
import com.hr.performancetracker.entity.PerformanceReview;
import com.hr.performancetracker.entity.ReviewCycle;
import com.hr.performancetracker.exception.ResourceNotFoundException;
import com.hr.performancetracker.repository.EmployeeRepository;
import com.hr.performancetracker.repository.PerformanceReviewRepository;
import com.hr.performancetracker.repository.ReviewCycleRepository;
import com.hr.performancetracker.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository          employeeRepository;
    private final ReviewCycleRepository       cycleRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheConfig.CYCLE_SUMMARY,
                    key   = "#request.cycleId"
            ),
            @CacheEvict(
                    value = CacheConfig.EMPLOYEE_REVIEWS,
                    key   = "#request.employeeId"
            ),
            @CacheEvict(
                    value      = CacheConfig.EMPLOYEE_FILTER,
                    allEntries = true
            )
    })
    public ReviewResponse submitReview(CreateReviewRequest request) {

        log.info("Submitting review: employeeId={}, cycleId={}, rating={}",
                request.getEmployeeId(),
                request.getCycleId(),
                request.getRating());

        Employee employee = employeeRepository
                .findById(request.getEmployeeId())
                .orElseThrow(() ->
                        ResourceNotFoundException.employee(
                                request.getEmployeeId()));

        ReviewCycle cycle = cycleRepository
                .findById(request.getCycleId())
                .orElseThrow(() ->
                        ResourceNotFoundException.cycle(
                                request.getCycleId()));

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .cycle(cycle)
                .rating(request.getRating())
                .reviewerNotes(request.getReviewerNotes())
                .reviewerName(request.getReviewerName())
                .build();

        PerformanceReview saved = reviewRepository.save(review);

        log.info("Review submitted: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    @Cacheable(
            value = CacheConfig.EMPLOYEE_REVIEWS,
            key   = "#employeeId"
    )
    @Transactional(readOnly = true)
    public List<ReviewResponse> getEmployeeReviews(Long employeeId) {

        log.debug("Fetching reviews for employeeId={}", employeeId);

        if (!employeeRepository.existsById(employeeId)) {
            throw ResourceNotFoundException.employee(employeeId);
        }

        return reviewRepository
                .findByEmployeeIdOrderBySubmittedAtDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ReviewResponse toResponse(PerformanceReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .employeeId(review.getEmployee().getId())
                .employeeName(review.getEmployee().getName())
                .cycleId(review.getCycle().getId())
                .cycleName(review.getCycle().getName())
                .cycleStartDate(review.getCycle().getStartDate())
                .cycleEndDate(review.getCycle().getEndDate())
                .rating(review.getRating())
                .reviewerNotes(review.getReviewerNotes())
                .reviewerName(review.getReviewerName())
                .submittedAt(review.getSubmittedAt())
                .build();
    }
}