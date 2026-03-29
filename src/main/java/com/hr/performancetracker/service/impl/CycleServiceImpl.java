package com.hr.performancetracker.service.impl;

import com.hr.performancetracker.config.CacheConfig;
import com.hr.performancetracker.dto.request.CreateCycleRequest;
import com.hr.performancetracker.dto.response.CycleResponse;
import com.hr.performancetracker.dto.response.CycleSummaryResponse;
import com.hr.performancetracker.entity.ReviewCycle;
import com.hr.performancetracker.exception.BusinessException;
import com.hr.performancetracker.exception.ResourceNotFoundException;
import com.hr.performancetracker.repository.GoalRepository;
import com.hr.performancetracker.repository.PerformanceReviewRepository;
import com.hr.performancetracker.repository.ReviewCycleRepository;
import com.hr.performancetracker.service.CycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CycleServiceImpl implements CycleService {

    private final ReviewCycleRepository       cycleRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final GoalRepository              goalRepository;

    @Override
    @Transactional
    public CycleResponse createCycle(CreateCycleRequest request) {

        log.info("Creating cycle: name={}", request.getName());

        if (cycleRepository.existsByName(request.getName())) {
            throw BusinessException.duplicateCycle(
                    request.getName());
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw BusinessException.invalidCycleDates();
        }

        ReviewCycle cycle = ReviewCycle.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        ReviewCycle saved = cycleRepository.save(cycle);

        log.info("Cycle created: id={}", saved.getId());

        return toCycleResponse(saved);
    }

    @Override
    @Cacheable(
            value = CacheConfig.CYCLE_SUMMARY,
            key   = "#cycleId"
    )
    @Transactional(readOnly = true)
    public CycleSummaryResponse getSummary(Long cycleId) {

        log.debug("Computing summary: cycleId={}", cycleId);

        ReviewCycle cycle = cycleRepository
                .findById(cycleId)
                .orElseThrow(() ->
                        ResourceNotFoundException.cycle(cycleId));

        // Query 1: Average rating
        Double avgRating = reviewRepository
                .findAverageRatingByCycleId(cycleId);

        // Query 2: Total review count
        Long totalReviews = reviewRepository
                .countByCycleId(cycleId);

        // Query 3: Top performer
        CycleSummaryResponse.TopPerformer topPerformer = null;

        List<Object[]> topResult = reviewRepository
                .findTopPerformerByCycleId(cycleId);

        if (!topResult.isEmpty()) {
            /*
             * FIX: use get(0) instead of getFirst()
             * getFirst() was added in Java 21
             * get(0) works in Java 17 and all versions
             */
            Object[] row = topResult.get(0);

            topPerformer = CycleSummaryResponse.TopPerformer
                    .builder()
                    .employeeId(
                            ((Number) row[0]).longValue())
                    .employeeName(
                            (String) row[1])
                    .department(
                            (String) row[2])
                    .averageRating(
                            Math.round(
                                    ((Number) row[3]).doubleValue()
                                            * 100.0) / 100.0)
                    .build();
        }

        // Query 4: Goal counts in one query
        List<Object[]> goalCountsList = goalRepository.getGoalCountsByCycleId(cycleId);

        long completed = 0L;
        long missed    = 0L;
        long pending   = 0L;
        long total     = 0L;

        if (goalCountsList != null && !goalCountsList.isEmpty()) {
            Object[] goalCounts = goalCountsList.get(0); // ✅ Get the first (only) row

            completed = goalCounts[0] != null
                    ? ((Number) goalCounts[0]).longValue() : 0L;
            missed    = goalCounts[1] != null
                    ? ((Number) goalCounts[1]).longValue() : 0L;
            pending   = goalCounts[2] != null
                    ? ((Number) goalCounts[2]).longValue() : 0L;
            total     = goalCounts[3] != null
                    ? ((Number) goalCounts[3]).longValue() : 0L;
        }

        Double roundedAvg = avgRating != null
                ? Math.round(avgRating * 100.0) / 100.0
                : null;

        log.debug("Summary computed: cycleId={}, avgRating={}, " +
                        "totalReviews={}, totalGoals={}",
                cycleId, roundedAvg, totalReviews, total);

        return CycleSummaryResponse.builder()
                .cycleId(cycle.getId())
                .cycleName(cycle.getName())
                .averageRating(roundedAvg)
                .topPerformer(topPerformer)
                .completedGoalsCount(completed)
                .missedGoalsCount(missed)
                .pendingGoalsCount(pending)
                .totalGoalsCount(total)
                .totalReviews(totalReviews)
                .build();
    }

    private CycleResponse toCycleResponse(ReviewCycle cycle) {
        return CycleResponse.builder()
                .id(cycle.getId())
                .name(cycle.getName())
                .startDate(cycle.getStartDate())
                .endDate(cycle.getEndDate())
                .createdAt(cycle.getCreatedAt())
                .build();
    }
}