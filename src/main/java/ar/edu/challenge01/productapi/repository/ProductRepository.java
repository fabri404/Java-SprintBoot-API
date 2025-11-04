package ar.edu.challenge01.productapi.repository;

import ar.edu.challenge01.productapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

