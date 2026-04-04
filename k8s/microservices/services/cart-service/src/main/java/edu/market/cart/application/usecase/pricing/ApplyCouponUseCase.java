package edu.market.cart.application.usecase.pricing;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.CouponCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.pricing.ApplyCouponUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para aplicar un cupón al carrito.
 *
 * Responsabilidades:
 * - Validar y orquestar la aplicación de un cupón.
 * - Persistir el agregado Cart después de la actualización.
 * - Registrar trazabilidad y devolver DTO de aplicación (CartResponse).
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class ApplyCouponUseCase implements ApplyCouponUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public ApplyCouponUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Aplica un cupón al carrito.
     *
     * @param command datos del cupón a aplicar
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    @Override
    public CartResponse apply(CouponCommand command, ClientContextCommand context) {
        log.info("apply - Init cartId=%s userId=%s coupon=%s context=%s",
                command.cartId(), command.userId(), command.coupon(), safeContext(context));

        try {
            log.debug("apply - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("apply - Call applyCouponToCart");
            applyCouponToCart(cart, command.coupon());

            log.debug("apply - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("apply - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("apply - End cartId=%s items=%s coupons=%s", saved.getId(), saved.getItems().size(), saved.getCoupons().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error applying coupon " + command.coupon() + " to cart " + command.cartId() + ": " + ex.getMessage();
            log.error("apply - Exception occurred cartId=%s userId=%s coupon=%s", command.cartId(), command.userId(), command.coupon(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private void applyCouponToCart(Cart cart, String couponCode) {
        log.debug("applyCouponToCart - Init coupon=%s", couponCode);
        // TODO: reemplazar con lógica real de dominio (validar cupón, calcular descuento, etc.)
        // Por ahora solo registra el código en la lista de cupones como placeholder
        var current = new java.util.ArrayList<>(cart.getCoupons());
        current.add(edu.market.cart.domain.vo.CouponAppliedVO.amount(couponCode, "placeholder", null, 1));
        cart.replaceCoupons(current);
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.clientIp(), context.userAgent());
    }
}
