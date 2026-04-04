package edu.market.cart.application.usecase.pricing;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.CouponCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.pricing.RemoveCouponUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.vo.CouponAppliedVO;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para remover un cupón del carrito.
 *
 * Responsabilidades:
 * - Orquestar la eliminación del cupón indicado.
 * - Persistir el agregado Cart después de la actualización.
 * - Registrar trazabilidad y devolver DTO de aplicación (CartResponse).
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class RemoveCouponUseCase implements RemoveCouponUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public RemoveCouponUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Remueve un cupón del carrito.
     *
     * @param command datos del cupón a remover
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    @Override
    public CartResponse remove(CouponCommand command, ClientContextCommand context) {
        log.info("remove - Init cartId=%s userId=%s coupon=%s context=%s",
                command.cartId(), command.userId(), command.coupon(), safeContext(context));

        try {
            log.debug("remove - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("remove - Call removeCouponFromCart");
            removeCouponFromCart(cart, command.coupon());

            log.debug("remove - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("remove - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("remove - End cartId=%s items=%s coupons=%s", saved.getId(), saved.getItems().size(), saved.getCoupons().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error removing coupon " + command.coupon() + " from cart " + command.cartId() + ": " + ex.getMessage();
            log.error("remove - Exception occurred cartId=%s userId=%s coupon=%s", command.cartId(), command.userId(), command.coupon(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private void removeCouponFromCart(Cart cart, String couponCode) {
        log.debug("removeCouponFromCart - Init coupon=%s current=%s", couponCode, cart.getCoupons().size());
        List<CouponAppliedVO> coupons = new java.util.ArrayList<>(cart.getCoupons());
        boolean removed = coupons.removeIf(c -> c.code().equalsIgnoreCase(couponCode));
        if (!removed) {
            throw new CartException("Coupon " + couponCode + " not found in cart " + cart.getId());
        }
        cart.replaceCoupons(coupons);
        log.debug("removeCouponFromCart - End removed=%s remaining=%s", removed, coupons.size());
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.clientIp(), context.userAgent());
    }
}
