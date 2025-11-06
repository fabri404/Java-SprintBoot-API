package ar.edu.challenge01.productapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "products")
public class Product {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank @Size(max = 255)
  @Column(nullable = false)
  private String name;

  @NotBlank
  @Column(nullable = false)
  private String description;

  @Positive
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal price;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist(){ if (createdAt == null) createdAt = Instant.now(); }

  // Getters/Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
