package ar.edu.challenge01.productapi.mapper;

import ar.edu.challenge01.productapi.dto.ProductRequest;
import ar.edu.challenge01.productapi.dto.ProductResponse;
import ar.edu.challenge01.productapi.entity.Product;

public final class ProductMapper {

    private ProductMapper() {}

    public static Product toEntity(ProductRequest req) {
        Product p = new Product();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        return p;
    }

    public static void updateEntity(Product p, ProductRequest req) {
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
    }

    public static ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getCreatedAt()
        );
    }
}

