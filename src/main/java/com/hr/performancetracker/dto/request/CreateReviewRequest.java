package com.hr.performancetracker.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Cycle ID is required")
    private Long cycleId;

    /*
     * Rating enforced at Java level here
     * Also enforced at DB level via CHECK constraint
     * Two layers of protection
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    /*
     * reviewerNotes is optional
     * No @NotBlank here
     * A manager can give a rating without notes
     */
    @Size(max = 5000,
            message = "Notes must not exceed 5000 characters")
    private String reviewerNotes;

    @NotBlank(message = "Reviewer name is required")
    @Size(max = 100,
            message = "Reviewer name must not exceed 100 characters")
    private String reviewerName;
}