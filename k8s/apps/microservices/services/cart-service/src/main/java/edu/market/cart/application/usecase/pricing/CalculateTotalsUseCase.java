package edu.market.cart.application.usecase.pricing;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.pricing.CalculateTotalsUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.model.CartItem;
import edu.market.cart.domain.model.CartTotals;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para recalcular totales del carrito.
 *
 * Responsabilidades:
 * - Calcular subtotal, descuentos, impuestos, envío y total final.
 * - Persistir el agregado Cart con los totales recalculados.
 * - Registrar trazabilidad y devolver DTO de aplicación (CartResponse).
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class CalculateTotalsUseCase implements CalculateTotalsUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public CalculateTotalsUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Calcula subtotal, impuestos, envío estimado y total final.
     *
     * @param command datos del carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con totales recalculados
     */
    @Override
    public CartResponse calculate(CartUserCommand command, ClientContextCommand context) {
        log.info("calculate - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));

        try {
            log.debug("calculate - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("calculate - Call recomputeTotals");
            CartTotals totals = recomputeTotals(cart);
            cart.replaceTotals(totals);

            log.debug("calculate - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("calculate - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("calculate - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error calculating totals for cart " + command.cartId() + ": " + ex.getMessage();
            log.error("calculate - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private CartTotals recomputeTotals(Cart cart) {
        log.debug("recomputeTotals - Init cartId=%s", cart.getId());
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discounts = BigDecimal.ZERO; // TODO: aplicar descuentos/cupons reales
        BigDecimal taxes = BigDecimal.ZERO;     // TODO: calcular impuestos reales
        BigDecimal shipping = BigDecimal.ZERO;  // TODO: estimar envío real
        BigDecimal total = subtotal.subtract(discounts).add(taxes).add(shipping);

        log.debug("recomputeTotals - End cartId=%s", cart.getId());
        return CartTotals.builder()
                .withSubtotal(subtotal)
                .withDiscounts(discounts)
                .withTaxes(taxes)
                .withShipping(shipping)
                .withTotal(total)
                .build();
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.clientIp(), context.userAgent());
    }
}
