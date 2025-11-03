package ar.edu.challenge01.productapi.controller;

import ar.edu.challenge01.productapi.dto.*;
import ar.edu.challenge01.productapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService service;
  public ProductController(ProductService service){ this.service = service; }

  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest r){
    var created = service.create(r);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(created.id()).toUri();
    return ResponseEntity.created(location).body(created); // 201
  }

  @GetMapping
  public ResponseEntity<Page<ProductResponse>> findAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,asc") String sort) {
    String[] s = sort.split(",");
    PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(s[1]), s[0]));
    return ResponseEntity.ok(service.findAll(pr)); // 200
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(@PathVariable Long id){
    return ResponseEntity.ok(service.findById(id)); // 200
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest r){
    return ResponseEntity.ok(service.update(id, r)); // 200
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id){
    service.delete(id);
    return ResponseEntity.noContent().build(); // 204
  }
}

