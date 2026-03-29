package com.hr.performancetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse implements Serializable {

    private Long   id;
    private Long   employeeId;
    private String employeeName;
    private Long   cycleId;
    private String cycleName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cycleStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cycleEndDate;

    private Integer rating;
    private String  reviewerNotes;
    private String  reviewerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
}