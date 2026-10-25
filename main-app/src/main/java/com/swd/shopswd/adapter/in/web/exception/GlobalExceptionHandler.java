package com.swd.shopswd.adapter.in.web.exception;

import com.swd.shopswd.adapter.in.web.dto.shared.ApiErrorResponse;
import com.swd.shopswd.domain.payment.exception.PaymentDeclinedException;
import com.swd.shopswd.domain.payment.exception.PaymentExternalException;
import com.swd.shopswd.domain.payment.exception.PaymentFailedException;
import com.swd.shopswd.domain.shared.exception.DomainException;
import com.swd.shopswd.domain.shared.exception.ResourceNotFoundException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(PaymentExternalException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentExternal(PaymentExternalException e) {
        log.error("[API] payment gateway error | code={} | message={}", e.getCode(), e.getMessage());
        return error(HttpStatus.BAD_GATEWAY, e.getCode(), "Payment service is temporarily unavailable. Please try again.");
    }

    @ExceptionHandler({PaymentDeclinedException.class, PaymentFailedException.class})
    public ResponseEntity<ApiErrorResponse> handlePaymentFailure(DomainException e) {
        return error(HttpStatus.PAYMENT_REQUIRED, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(DomainException e) {
        return error(HttpStatus.CONFLICT, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<ApiErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ApiErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse("INVALID_INPUT", "Request validation failed", errors));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_INPUT", e.getMessage());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleConcurrency(Exception e) {
        log.warn("[API] concurrent modification | {}", e.getMessage());
        return error(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
            "The request conflicted with another request. Please try again.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception e) {
        log.error("[API] unexpected error", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(code, message));
    }
}
