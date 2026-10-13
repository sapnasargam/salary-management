package com.acme.salary.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Maps exceptions to RFC 7807 problem responses. Field-level problems carry an {@code errors}
 * object ({@code field -> message}) that the UI binds to form controls.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return validationProblem(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    /** Constraint violations on query/path parameters, e.g. {@code size=1000}. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleParameterValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ParameterValidationResult result : ex.getAllValidationResults()) {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error -> errors.putIfAbsent(name, error.getDefaultMessage()));
        }
        return validationProblem(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(FieldValidationException.class)
    ProblemDetail handleFieldValidation(FieldValidationException ex) {
        HttpStatus status = ex.isConflict() ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return validationProblem(status, ex.getMessage(), Map.of(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail handleMalformedRequest(Exception ex) {
        log.debug("Malformed request", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request");
    }

    /** Last line of defence for the unique e-mail constraint when two requests race past the service check. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The change conflicts with existing data");
    }

    private static ProblemDetail validationProblem(HttpStatus status, String detail, Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("errors", errors);
        return problem;
    }
}
