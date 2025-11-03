package ar.edu.challenge01.productapi.mapper;

import ar.edu.challenge01.productapi.dto.*;
import ar.edu.challenge01.productapi.entity.Product;

public final class ProductMapper {
  private ProductMapper(){}

  public static Product toEntity(CreateProductRequest r){
    Product p = new Product();
    p.setName(r.name());
    p.setDescription(r.description());
    p.setPrice(r.price());
    return p;
  }

  public static void applyUpdate(Product p, UpdateProductRequest r){
    p.setName(r.name());
    p.setDescription(r.description());
    p.setPrice(r.price());
  }

  public static ProductResponse toResponse(Product p){
    return new ProductResponse(
      p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCreatedAt()
    );
  }
}

