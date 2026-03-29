package com.hr.performancetracker.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/*
 * @RestControllerAdvice handles exceptions for ALL controllers
 *
 * Without this: every controller needs try catch blocks
 * With this: controllers stay clean
 * All errors handled in one place consistently
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --------------------------------------------------------
    // 404 NOT FOUND
    // --------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value()
                ));
    }

    // --------------------------------------------------------
    // 400 BAD REQUEST — business rule violations
    // --------------------------------------------------------
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    // --------------------------------------------------------
    // 400 BAD REQUEST — @Valid annotation failures
    // Returns field level error messages
    // --------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        log.warn("Validation failed - field errors: {}", fieldErrors);

        ErrorResponse response = ErrorResponse.builder()
                .message("Validation failed. Check field errors.")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // --------------------------------------------------------
    // 400 BAD REQUEST — missing required @RequestParam
    // --------------------------------------------------------
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex) {

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        "Required parameter missing: "
                                + ex.getParameterName(),
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    // --------------------------------------------------------
    // 500 INTERNAL SERVER ERROR — unexpected errors
    // --------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex) {

        // ✅ Log full stack trace for debugging
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        // ✅ Show actual error message in response
        String message = ex.getMessage() != null
                ? ex.getMessage()
                : ex.getClass().getSimpleName() + " occurred";

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        message,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }

    // --------------------------------------------------------
    // Consistent error response shape for ALL errors
    // --------------------------------------------------------
    @Getter
    @Builder
    public static class ErrorResponse {

        private String              message;
        private int                 status;
        private LocalDateTime       timestamp;
        private Map<String, String> fieldErrors;

        public static ErrorResponse of(
                String message, int status) {

            return ErrorResponse.builder()
                    .message(message)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}