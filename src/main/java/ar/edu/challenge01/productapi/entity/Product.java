package ar.edu.challenge01.productapi.entity;

import java.math.RoundingMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false)
  private String name;

  @NotBlank
  @Column(nullable = false)
  private String description;

  @DecimalMin(value = "0.00", inclusive = true, message = "price must be greater than or equal to 0.00")
  @Digits(integer = 13, fraction = 2,
          message = "price must have up to 13 integer digits and 2 decimals")
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal price;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  // Getters/Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public BigDecimal getPrice() { return price; }

  public void setPrice(BigDecimal price) {
    if (price == null) {
      this.price = null;
    } else {
      // Siempre 2 decimales, redondeo HALF_UP (comportamiento típico de precios)
      this.price = price.setScale(2, RoundingMode.HALF_UP);
    }
  }
 }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

