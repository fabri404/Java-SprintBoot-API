package ar.edu.challenge01.productapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "name must not be blank")
        @Size(min = 3, max = 255, message = "name must be between 3 and 255 characters")
        @Pattern(
                regexp = ".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*",
                message = "name must contain at least one letter"
        )
        String name,

        @NotBlank(message = "description must not be blank")
        @Size(min = 3, max = 1000, message = "description must be between 3 and 1000 characters")
        @Pattern(
                regexp = ".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*",
                message = "description must contain at least one letter"
        )
        String description,

        @NotNull(message = "price must not be null")
        @PositiveOrZero(message = "price must be greater than or equal to 0")
        @Digits(integer = 13, fraction = 2,
                message = "price must have up to 13 integer digits and 2 decimals")
        BigDecimal price
) {}

