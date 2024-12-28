package com.randomlake.library.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(GeneralException.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException ex) {
    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getType());
    return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<String> handleValidationExceptions(
      ConstraintViolationException ex, WebRequest request) {
    String errorMessage =
        ex.getConstraintViolations().stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .reduce((s1, s2) -> s1 + "; " + s2)
            .orElse("Validation error");
    return ResponseEntity.badRequest().body(errorMessage);
  }
}
