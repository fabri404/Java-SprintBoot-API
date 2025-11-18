package ar.edu.challenge01.productapi.web;

import ar.edu.challenge01.productapi.dto.CreateProductRequest;
import ar.edu.challenge01.productapi.dto.UpdateProductRequest;
import ar.edu.challenge01.productapi.dto.ProductResponse;
import ar.edu.challenge01.productapi.entity.Product;
import ar.edu.challenge01.productapi.mapper.ProductMapper;
import ar.edu.challenge01.productapi.repository.ProductRepository;
// ajusta este import al nombre de tu excepción real
import ar.edu.challenge01.productapi.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /products
     * Devuelve todos los productos ordenados cronológicamente (más nuevos primero)
     * en formato ProductResponse.
     */
    @GetMapping
    public List<ProductResponse> list() {
        List<Product> products = repo.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return products.stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    /**
     * GET /products/{id}
     * Devuelve un producto o lanza excepción de no encontrado.
     */
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product with id " + id + " not found"));
        return ProductMapper.toResponse(product);
    }

    /**
     * POST /products
     * Crea un producto a partir de CreateProductRequest y devuelve ProductResponse.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest body
    ) {
        Product toSave = ProductMapper.toEntity(body);
        Product saved = repo.save(toSave);

        ProductResponse response = ProductMapper.toResponse(saved);

        return ResponseEntity
                .created(URI.create("/products/" + saved.getId()))
                .body(response);
    }

    /**
     * PUT /products/{id}
     * Actualiza completamente un producto existente.
     */
    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest body
    ) {
        Product updated = repo.findById(id)
                .map(existing -> {
                    ProductMapper.updateEntity(existing, body);
                    return repo.save(existing);
                })
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product with id " + id + " not found"));

        return ProductMapper.toResponse(updated);
    }

    /**
     * DELETE /products/{id}
     * Elimina un producto. Si no existe, lanza excepción de no encontrado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Product with id " + id + " not found");
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

