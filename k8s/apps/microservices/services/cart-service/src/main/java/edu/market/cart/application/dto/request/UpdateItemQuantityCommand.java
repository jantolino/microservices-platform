package edu.market.cart.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command para actualizar cantidad de un ítem.
 */
public record UpdateItemQuantityCommand(
        @NotNull(message = "cartId required")
        UUID cartId,
        @NotNull(message = "userId required")
        UUID userId,
        @NotNull(message = "itemId required")
        UUID itemId,
        @Min(value = 1, message = "quantity must be > 0")
        int quantity
) {}
