package ar.edu.challenge01.productapi.web;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

  // 404
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", "NOT_FOUND", "message", ex.getMessage()));
  }

  // 400 - Bean Validation (agrupa múltiples mensajes por campo)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    var fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.groupingBy(
            fe -> fe.getField(),
            LinkedHashMap::new,
            Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
        ));
    return ResponseEntity.badRequest().body(Map.of(
        "error", "BAD_REQUEST",
        "message", "Validation failed",
        "fields", fields
    ));
  }

  // 400 - JSON malformado
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleBadJson(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest()
        .body(Map.of("error", "BAD_REQUEST", "message", "Malformed JSON"));
  }

  // 409 - Conflictos de integridad (unique, FK, etc.)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "CONFLICT", "message", "Data integrity violation"));
  }

  // 409 - Conflicto de negocio explícito (si usás tu propia excepción)
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<?> handleConflictExplicit(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "CONFLICT", "message", ex.getMessage()));
  }

  // 500 - Genérico
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "Unexpected error"));
  }
}

