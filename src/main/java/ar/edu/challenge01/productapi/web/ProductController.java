package ar.edu.challenge01.productapi.web;

import ar.edu.challenge01.productapi.entity.Product;
import ar.edu.challenge01.productapi.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
  private final ProductRepository repo;
  public ProductController(ProductRepository repo) { this.repo = repo; }

  @GetMapping
  public List<Product> list() {
    return repo.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> get(@PathVariable Long id) {
    return repo.findById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Product> create(@RequestBody Product p) {
    // validaciones mínimas
    if (p.getName() == null || p.getDescription() == null || p.getPrice() == null) {
      return ResponseEntity.badRequest().build();
    }
    Product saved = repo.save(p);
    return ResponseEntity.created(URI.create("/products/" + saved.getId())).body(saved);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product p) {
    return repo.findById(id).map(db -> {
      db.setName(p.getName());
      db.setDescription(p.getDescription());
      db.setPrice(p.getPrice());
      Product saved = repo.save(db);
      return ResponseEntity.ok(saved);
    }).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}

