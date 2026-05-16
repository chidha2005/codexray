package com.codexray.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CodeAnalysisException.class)
    public ResponseEntity<ApiErrorResponse> handleCodeAnalysisException(CodeAnalysisException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), "CODE_ANALYSIS_FAILED", ex.getMessage(), request.getRequestURI(), List.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), "VALIDATION_FAILED", "Invalid request payload", request.getRequestURI(), details
        ));
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiErrorResponse> handleConstraintException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), "VALIDATION_FAILED", ex.getMessage(), request.getRequestURI(), List.of()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR", "Unexpected server error occurred", request.getRequestURI(), List.of()
        ));
    }
}
