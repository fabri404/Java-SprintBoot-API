package ar.edu.challenge01.productapi.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String,Object>> notFound(NotFoundException ex, WebRequest req){
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
      "timestamp", Instant.now().toString(),
      "status", 404, "error", "Not Found",
      "message", ex.getMessage(),
      "path", req.getDescription(false).replace("uri=","")
    ));
  }

  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String,Object>> badRequest(org.springframework.web.bind.MethodArgumentNotValidException ex, WebRequest req){
    var details = ex.getBindingResult().getFieldErrors().stream()
      .collect(java.util.stream.Collectors.toMap(fe -> fe.getField(), fe -> fe.getDefaultMessage(), (a,b)->a));
    return ResponseEntity.badRequest().body(Map.of(
      "timestamp", Instant.now().toString(),
      "status", 400, "error", "Bad Request",
      "message", "Validation failed",
      "details", details,
      "path", req.getDescription(false).replace("uri=","")
    ));
  }
}


