package ar.edu.challenge01.productapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String name,
        @NotBlank String description,
        @PositiveOrZero BigDecimal price
) {}

