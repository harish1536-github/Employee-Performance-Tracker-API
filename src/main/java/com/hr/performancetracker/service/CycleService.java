package com.hr.performancetracker.service;

import com.hr.performancetracker.dto.request.CreateCycleRequest;
import com.hr.performancetracker.dto.response.CycleResponse;
import com.hr.performancetracker.dto.response.CycleSummaryResponse;

/*
 * Cycle service contract
 * Controller depends on this interface
 * CycleServiceImpl provides the actual implementation
 */
public interface CycleService {

    CycleResponse createCycle(CreateCycleRequest request);

    CycleSummaryResponse getSummary(Long cycleId);
}