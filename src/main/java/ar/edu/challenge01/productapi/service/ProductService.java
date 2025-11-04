package ar.edu.challenge01.productapi.service;

import ar.edu.challenge01.productapi.dto.ProductRequest;
import ar.edu.challenge01.productapi.dto.ProductResponse;
import ar.edu.challenge01.productapi.entity.Product;
import ar.edu.challenge01.productapi.mapper.ProductMapper;
import ar.edu.challenge01.productapi.repository.ProductRepository;
import ar.edu.challenge01.productapi.web.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public ProductResponse create(ProductRequest req) {
        Product p = ProductMapper.toEntity(req);
        p = repo.save(p);
        return ProductMapper.toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repo.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductMapper.toResponse(findEntity(id));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product p = findEntity(id);
        ProductMapper.updateEntity(p, req);
        p = repo.save(p);
        return ProductMapper.toResponse(p);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Product %d not found".formatted(id));
        }
        repo.deleteById(id);
    }

    // Helper interno
    private Product findEntity(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product %d not found".formatted(id)));
    }
}

