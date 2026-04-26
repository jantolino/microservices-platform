package edu.market.cart.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command genérico para aplicar o remover cupones.
 */
public record CouponCommand(
        @NotNull(message = "cartId required")
        UUID cartId,
        @NotNull(message = "userId required")
        UUID userId,
        @NotBlank(message = "coupon required")
        String coupon
) {}
