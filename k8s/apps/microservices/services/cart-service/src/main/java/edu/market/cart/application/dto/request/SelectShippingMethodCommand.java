package edu.market.cart.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command para seleccionar método de envío.
 */
public record SelectShippingMethodCommand(
        @NotNull(message = "cartId required")
        UUID cartId,
        @NotNull(message = "userId required")
        UUID userId,
        @NotBlank(message = "methodId required")
        String methodId
) {}
