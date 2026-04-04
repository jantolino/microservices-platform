package edu.market.cart.application.usecase.inventory;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.inventory.MarkUnavailableItemsUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.model.CartItem;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para marcar ítems no disponibles (stock/precio).
 *
 * Responsabilidades:
 * - Detectar ítems sin stock o con precio inválido (placeholder).
 * - Marcarlos/removerlos según la regla (placeholder) y persistir el resultado.
 * - Devolver DTO de aplicación.
 */
public class MarkUnavailableItemsUseCase implements MarkUnavailableItemsUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public MarkUnavailableItemsUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Marca como no disponibles los ítems afectados (sin stock o con cambio de precio).
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    @Override
    public CartResponse markUnavailable(CartUserCommand command, ClientContextCommand context) {
        log.info("markUnavailable - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));
        try {
            log.debug("markUnavailable - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("markUnavailable - Call flagUnavailableItems (TODO)");
            List<CartItem> updated = flagUnavailableItems(cart);
            cart.replaceItems(updated);

            log.debug("markUnavailable - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("markUnavailable - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("markUnavailable - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error marking unavailable items for cart " + command.cartId() + ": " + ex.getMessage();
            log.error("markUnavailable - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
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

    private List<CartItem> flagUnavailableItems(Cart cart) {
        log.debug("flagUnavailableItems - Init items=%s", cart.getItems().size());
        // TODO: integrar con servicio de inventario/precios para determinar indisponibilidad.
        // Placeholder: devuelve mismos ítems sin modificación.
        List<CartItem> result = new ArrayList<>(cart.getItems());
        log.debug("flagUnavailableItems - End items=%s", result.size());
        return result;
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
