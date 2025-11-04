package ar.edu.challenge01.productapi.web;

import ar.edu.challenge01.productapi.dto.ProductRequest;
import ar.edu.challenge01.productapi.dto.ProductResponse;
import ar.edu.challenge01.productapi.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService service;

  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest req) {
    ProductResponse body = service.create(req);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(body.id()).toUri();
    return ResponseEntity.created(location).body(body); // 201 + Location
  }

  @GetMapping
  public List<ProductResponse> list() {
    return service.findAll(); // 200 OK
  }

  @GetMapping("/{id}")
  public ProductResponse get(@PathVariable Long id) {
    return service.findById(id); // 200 OK o 404 por excepción
  }

  @PutMapping("/{id}")
  public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
    return service.update(id, req); // 200 OK con cuerpo
    // Si preferís 204 sin cuerpo:
    // service.update(id, req); return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build(); // 204
  }
}

