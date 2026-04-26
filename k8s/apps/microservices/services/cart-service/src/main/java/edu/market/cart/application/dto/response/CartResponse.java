package edu.market.cart.application.dto.response;

import edu.market.cart.domain.enums.CartStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida principal para el carrito.
 */
public record CartResponse(
        UUID id,
        UUID userId,
        String sessionId,
        CartStatus status,
        List<CartItemResponse> items,
        List<String> coupons,
        CartTotalsResponse totals,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
