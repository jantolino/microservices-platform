package edu.market.cart.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command para agregar un ítem al carrito.
 */
public record AddItemCommand(
        @NotNull(message = "cartId required")
        UUID cartId,
        @NotNull(message = "userId required")
        UUID userId,
        @NotBlank(message = "productId required")
        String productId,
        String variantId,
        @Min(value = 1, message = "quantity must be > 0")
        int quantity,
        @NotNull(message = "unitPrice required")
        BigDecimal unitPrice
) {}
