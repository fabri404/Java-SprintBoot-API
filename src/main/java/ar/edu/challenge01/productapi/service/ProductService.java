package ar.edu.challenge01.productapi.service;

import ar.edu.challenge01.productapi.dto.*;
import ar.edu.challenge01.productapi.entity.Product;
import ar.edu.challenge01.productapi.exception.NotFoundException;
import ar.edu.challenge01.productapi.mapper.ProductMapper;
import ar.edu.challenge01.productapi.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
  private final ProductRepository repo;
  public ProductService(ProductRepository repo){ this.repo = repo; }

  @Transactional
  public ProductResponse create(CreateProductRequest r){
    Product p = ProductMapper.toEntity(r);
    return ProductMapper.toResponse(repo.save(p));
  }

  @Transactional(readOnly = true)
  public Page<ProductResponse> findAll(Pageable pageable){
    return repo.findAll(pageable).map(ProductMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public ProductResponse findById(Long id){
    Product p = repo.findById(id)
      .orElseThrow(() -> new NotFoundException("Product %d not found".formatted(id)));
    return ProductMapper.toResponse(p);
  }

  @Transactional
  public ProductResponse update(Long id, UpdateProductRequest r){
    Product p = repo.findById(id)
      .orElseThrow(() -> new NotFoundException("Product %d not found".formatted(id)));
    ProductMapper.applyUpdate(p, r);
    return ProductMapper.toResponse(repo.save(p));
  }

  @Transactional
  public void delete(Long id){
    if (!repo.existsById(id)) throw new NotFoundException("Product %d not found".formatted(id));
    repo.deleteById(id);
  }
}

