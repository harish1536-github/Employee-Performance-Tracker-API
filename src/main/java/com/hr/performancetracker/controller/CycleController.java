package com.hr.performancetracker.controller;

import com.hr.performancetracker.dto.request.CreateCycleRequest;
import com.hr.performancetracker.dto.response.CycleResponse;
import com.hr.performancetracker.dto.response.CycleSummaryResponse;
import com.hr.performancetracker.service.CycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * No Swagger annotations
 * Clean controller
 */
@RestController
@RequestMapping("/cycles")
@RequiredArgsConstructor
public class CycleController {

    private final CycleService cycleService;

    /*
     * POST /cycles
     * Creates a new review cycle
     * Returns 201 Created
     */
    @PostMapping
    public ResponseEntity<CycleResponse> create(
            @Valid @RequestBody CreateCycleRequest request) {

        CycleResponse response =
                cycleService.createCycle(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * GET /cycles/{id}/summary
     * Returns avg rating, top performer, goal counts
     * Returns 200 OK or 404 if cycle not found
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<CycleSummaryResponse> getSummary(
            @PathVariable Long id) {

        return ResponseEntity.ok(cycleService.getSummary(id));
    }
}