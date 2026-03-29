package com.hr.performancetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
 * Response for GET /cycles/{id}/summary
 * Contains aggregated data from reviews and goals
 * This is the most expensive endpoint so it is cached
 * for 5 minutes in Redis
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleSummaryResponse implements Serializable {

    private Long   cycleId;
    private String cycleName;

    /*
     * Average of all ratings in this cycle
     * Rounded to 2 decimal places in service layer
     * null if no reviews exist yet
     */
    private Double averageRating;

    /*
     * Employee with highest average rating in cycle
     * null if no reviews exist yet
     */
    private TopPerformer topPerformer;

    private Long completedGoalsCount;
    private Long missedGoalsCount;
    private Long pendingGoalsCount;
    private Long totalGoalsCount;
    private Long totalReviews;

    /*
     * Static nested class
     * Static means it does not need instance of outer class
     * Nested because it only makes sense inside CycleSummaryResponse
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformer implements Serializable {
        private Long   employeeId;
        private String employeeName;
        private String department;
        private Double averageRating;
    }
}