package edu.market.cart.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Command genérico para operaciones que solo requieren cartId y userId.
 */
public record CartUserCommand(
        @NotNull(message = "cartId required")
        UUID cartId,
        @NotNull(message = "userId required")
        UUID userId
) {}
