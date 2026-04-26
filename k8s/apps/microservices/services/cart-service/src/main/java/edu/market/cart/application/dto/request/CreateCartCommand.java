package edu.market.cart.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Command para crear un carrito (usuario autenticado o sesión invitada).
 */
public record CreateCartCommand(
        @NotNull(message = "userId required")
        UUID userId,

        @NotNull(message = "sessionId required")
        String sessionId
) {}
