package edu.market.cart.application.mapper;

import edu.market.cart.application.dto.response.CartItemResponse;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.dto.response.CartTotalsResponse;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.model.CartItem;
import edu.market.cart.domain.model.CartTotals;
import edu.market.cart.domain.vo.CouponAppliedVO;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entidades del dominio de carrito a DTOs de respuesta de la capa aplicación.
 * Mantiene la separación de capas evitando exponer modelos de dominio hacia el exterior.
 */
public final class CartMapper {

    private CartMapper() {
    }

    /**
     * Convierte un agregado Cart a su representación CartResponse.
     *
     * @param cart entidad de dominio
     * @return DTO de respuesta o null si la entrada es null
     */
    public static CartResponse toResponse(Cart cart) {
        if (cart == null) {
            return null;
        }
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getSessionId(),
                cart.getStatus(),
                mapItems(cart.getItems()),
                mapCoupons(cart.getCoupons()),
                mapTotals(cart.getTotals()),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private static List<CartItemResponse> mapItems(List<CartItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .map(CartMapper::mapItem)
                .collect(Collectors.toList());
    }

    private static CartItemResponse mapItem(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    private static List<String> mapCoupons(List<CouponAppliedVO> coupons) {
        if (coupons == null) {
            return Collections.emptyList();
        }
        return coupons.stream()
                .filter(Objects::nonNull)
                .map(CouponAppliedVO::code)
                .collect(Collectors.toList());
    }

    private static CartTotalsResponse mapTotals(CartTotals totals) {
        return Optional.ofNullable(totals)
                .map(t -> new CartTotalsResponse(
                        t.getSubtotal(),
                        t.getDiscounts(),
                        t.getTaxes(),
                        t.getShipping(),
                        t.getTotal()
                ))
                .orElse(null);
    }
}
