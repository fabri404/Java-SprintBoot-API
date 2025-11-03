package ar.edu.challenge01.productapi.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
  @NotBlank String name,
  @NotBlank String description,
  @PositiveOrZero BigDecimal price
) {}

