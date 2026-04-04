package edu.market.cart.application.dto.response;

import java.math.BigDecimal;

/**
 * DTO de salida para los totales del carrito.
 */
public record CartTotalsResponse(
        BigDecimal subtotal,
        BigDecimal discounts,
        BigDecimal taxes,
        BigDecimal shipping,
        BigDecimal total
) {}
