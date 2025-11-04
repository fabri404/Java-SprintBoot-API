package ar.edu.challenge01.productapi.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", "NOT_FOUND", "message", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    var fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            fe -> fe.getField(),
            fe -> fe.getDefaultMessage(),
            (a, b) -> a, LinkedHashMap::new));
    return ResponseEntity.badRequest()
        .body(Map.of("error", "BAD_REQUEST", "message", "Validation failed", "fields", fields));
  }
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<?> handleConflictExplicit(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error","CONFLICT","message",ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleBadJson(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest()
        .body(Map.of("error", "BAD_REQUEST", "message", "Malformed JSON"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleConflict(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "CONFLICT", "message", "Data integrity violation"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "Unexpected error"));
  }
}

