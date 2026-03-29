package com.hr.performancetracker.service.impl;

import com.hr.performancetracker.config.CacheConfig;
import com.hr.performancetracker.dto.request.CreateEmployeeRequest;
import com.hr.performancetracker.dto.response.EmployeeResponse;
import com.hr.performancetracker.entity.Employee;
import com.hr.performancetracker.exception.BusinessException;
import com.hr.performancetracker.exception.ResourceNotFoundException;
import com.hr.performancetracker.repository.EmployeeRepository;
import com.hr.performancetracker.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * @Service marks this as a Spring managed bean
 *
 * "implements EmployeeService" means:
 * This class MUST provide all methods defined in the interface
 * If any method is missing, code will not compile
 *
 * Spring sees:
 * - EmployeeService interface exists
 * - EmployeeServiceImpl implements it and has @Service
 * - When controller asks for EmployeeService, inject this class
 *
 * This is called Dependency Injection with interface abstraction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    /*
     * @Override means:
     * This method is fulfilling the contract defined in EmployeeService
     * If the interface changes the method signature,
     * compiler will immediately tell us here to update too
     */
    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.EMPLOYEE_FILTER, allEntries = true)
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {

        log.info("Creating employee: name={}, department={}",
                request.getName(), request.getDepartment());

        if (employeeRepository.existsByNameAndDepartment(
                request.getName(), request.getDepartment())) {
            throw BusinessException.duplicateEmployee(
                    request.getName(), request.getDepartment());
        }

        Employee employee = Employee.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .role(request.getRole())
                .joiningDate(request.getJoiningDate())
                .build();

        Employee saved = employeeRepository.save(employee);

        log.info("Employee created successfully: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {

        log.debug("Fetching employee: id={}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.employee(id));

        return toResponse(employee);
    }

    @Override
    @Cacheable(
            value = CacheConfig.EMPLOYEE_FILTER,
            key   = "#department + '-' + #minRating"
    )
    @Transactional(readOnly = true)
    public List<EmployeeResponse> filter(
            String department, Double minRating) {

        log.debug("Filtering employees: department={}, minRating={}",
                department, minRating);

        return employeeRepository
                .findByDepartmentAndMinRating(department, minRating)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * Private helper method
     * Converts Employee entity to EmployeeResponse DTO
     * Private because it is an internal detail of this implementation
     * The interface does not know about this method
     * Other classes cannot call it
     */
    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .department(employee.getDepartment())
                .role(employee.getRole())
                .joiningDate(employee.getJoiningDate())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}