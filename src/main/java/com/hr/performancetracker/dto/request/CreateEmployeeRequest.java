package com.hr.performancetracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/*
 * What client sends in POST /employees request body
 *
 * Separate from Employee entity because:
 * Entity has id, createdAt, updatedAt client should not set
 * We validate here without polluting entity
 * API contract stays stable even if DB schema changes
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateEmployeeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Department is required")
    @Size(max = 100,
            message = "Department must not exceed 100 characters")
    private String department;

    @NotBlank(message = "Role is required")
    @Size(max = 100, message = "Role must not exceed 100 characters")
    private String role;

    @NotNull(message = "Joining date is required")
    @PastOrPresent(
            message = "Joining date cannot be in the future")
    private LocalDate joiningDate;
}