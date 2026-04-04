package edu.market.cart.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de salida para líneas del carrito.
 */
public record CartItemResponse(
        UUID id,
        String productId,
        String variantId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {}
