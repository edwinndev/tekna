package com.tekna.api.config;

import com.tekna.api.common.exception.ApplicationErrorResponse;
import com.tekna.api.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApplicationErrorResponse> handleApplicationException(ApplicationException ex) {
        log.warn("Application exception occurred: {}", ex.getMessage());
        ApplicationErrorResponse response = new ApplicationErrorResponse(
                ex.getStatus().value(),
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApplicationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation error");

        log.warn("Validation failed: {}", errorMessage);

        ApplicationErrorResponse response = new ApplicationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);

        ApplicationErrorResponse response = new ApplicationErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
