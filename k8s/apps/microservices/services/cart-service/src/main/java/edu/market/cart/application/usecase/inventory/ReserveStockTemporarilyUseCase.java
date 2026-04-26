package edu.market.cart.application.usecase.inventory;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.inventory.ReserveStockTemporarilyUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para reservar stock temporalmente durante una ventana corta.
 *
 * Responsabilidades:
 * - Validar y bloquear el carrito si aplica (placeholder).
 * - Invocar a la capa de inventario (TODO) y reflejar el resultado en el carrito.
 * - Persistir y devolver DTO de aplicación.
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class ReserveStockTemporarilyUseCase implements ReserveStockTemporarilyUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public ReserveStockTemporarilyUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Solicita la reserva temporal de stock para los ítems del carrito.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con el estado de reserva reflejado (si aplica)
     */
    @Override
    public CartResponse reserve(CartUserCommand command, ClientContextCommand context) {
        log.info("reserve - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));
        try {
            log.debug("reserve - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("reserve - Call performReservation (TODO)");
            performReservation(cart);

            log.debug("reserve - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("reserve - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("reserve - End cartId=%s", saved.getId());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error reserving stock for cart " + command.cartId() + ": " + ex.getMessage();
            log.error("reserve - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        Cart result = cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId));
        if (result.getUserId() != null && !result.getUserId().equals(userId)) {
            throw new CartException("Cart " + cartId + " does not belong to user " + userId);
        }
        return result;
    }

    private void performReservation(Cart cart) {
        // TODO: invocar servicio de inventario para reservar stock y reflejar estado en ítems
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
