package com.woridge.chatbot.exception;

import com.woridge.chatbot.dto.ErrorResponse;
import com.woridge.chatbot.enumeration.ErrorCode;
import com.woridge.chatbot.exception.chat.*;
import com.woridge.chatbot.exception.classifier.*;
import com.woridge.chatbot.exception.data.*;
import com.woridge.chatbot.exception.ingestion.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse<String>>
    handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied : {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.ACCESS_DENIED
                ));
    }

    @ExceptionHandler(TenantMissingException.class)
    public ResponseEntity<ErrorResponse<String>>
    handleTenantMissing(TenantMissingException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.TENANT_MISSING
                ));
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>>
    handleEmployeeNotFound(
            EmployeeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.EMPLOYEE_NOT_FOUND
                ));
    }

    @ExceptionHandler(StatsNotAvailableException.class)
    public ResponseEntity<ErrorResponse<String>>
    handleStatsNotAvailable(
            StatsNotAvailableException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.STATS_NOT_AVAILABLE
                ));
    }

    @ExceptionHandler(
            ClassifierUnavailableException.class
    )
    public ResponseEntity<ErrorResponse<String>>
    handleClassifierUnavailable(
            ClassifierUnavailableException ex) {
        log.error("Classifier unavailable : {}",
                ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.CLASSIFIER_UNAVAILABLE
                ));
    }

    @ExceptionHandler(IngestionFailedException.class)
    public ResponseEntity<ErrorResponse<String>>
    handleIngestionFailed(
            IngestionFailedException ex) {
        log.error("Ingestion failed : {}",
                ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.INGESTION_FAILED
                ));
    }

    @ExceptionHandler(FileEmptyException.class)
    public ResponseEntity<ErrorResponse<String>>
    handleFileEmpty(FileEmptyException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.FILE_EMPTY
                ));
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ErrorResponse<String>>
    handleValidation(
            MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField()
                        + " : "
                        + e.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.createErrorResponse(
                        details,
                        ErrorCode.VALIDATION_ERROR
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<String>>
    handleGeneral(Exception ex) {
        log.error("Erreur inattendue : {}",
                ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.INTERNAL_ERROR
                ));
    }
}