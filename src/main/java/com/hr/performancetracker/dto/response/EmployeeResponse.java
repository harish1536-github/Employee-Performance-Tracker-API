package com.hr.performancetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Serializable required for Redis storage
 *
 * @JsonFormat controls how dates appear in JSON
 * Without it LocalDate becomes [2022, 3, 15]
 * With it LocalDate becomes "2022-03-15"
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse implements Serializable {

    private Long   id;
    private String name;
    private String department;
    private String role;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}