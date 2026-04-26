package edu.market.cart.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command para eliminar un ítem del carrito.
 */
public record RemoveItemCommand(
        @NotNull(message = "cartId required")
        UUID cartId,
        @NotNull(message = "userId required")
        UUID userId,
        @NotNull(message = "itemId required")
        UUID itemId
) {}
