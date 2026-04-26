package edu.market.cart.application.usecase.cart;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.RemoveItemCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.cart.RemoveItemFromCartUseCasePort;
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
 * Caso de uso para eliminar un ítem del carrito.
 *
 * Responsabilidades:
 * - Orquestar la eliminación de una línea del carrito.
 * - Persistir el agregado Cart después del cambio.
 * - Registrar trazabilidad y devolver DTO de aplicación (CartResponse).
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper) para no exponer dominio.
 * - Manejo de errores centralizado con ApplicationException y logging detallado.
 */
public class RemoveItemFromCartUseCase implements RemoveItemFromCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public RemoveItemFromCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Elimina el ítem indicado del carrito.
     *
     * @param command datos del ítem a eliminar
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    @Override
    public CartResponse removeItem(RemoveItemCommand command, ClientContextCommand context) {
        log.info("removeItem - Init cartId=%s userId=%s itemId=%s context=%s",
                command.cartId(), command.userId(), command.itemId(), safeContext(context));

        try {
            log.debug("removeItem - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("removeItem - Call applyRemoval");
            cart.replaceItems(applyRemoval(cart, command));

            log.debug("removeItem - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("removeItem - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("removeItem - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error removing item " + command.itemId() + " from cart " + command.cartId() + ": " + ex.getMessage();
            log.error("removeItem - Exception occurred cartId=%s userId=%s itemId=%s", command.cartId(), command.userId(), command.itemId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private List<CartItem> applyRemoval(Cart cart, RemoveItemCommand command) {
        log.debug("applyRemoval - Init items=%s targetItem=%s", cart.getItems().size(), command.itemId());
        List<CartItem> updated = new ArrayList<>();
        boolean removed = false;

        for (CartItem current : cart.getItems()) {
            if (current.getId().equals(command.itemId())) {
                removed = true;
                continue;
            }
            updated.add(current);
        }

        if (!removed) {
            throw new CartException("Item " + command.itemId() + " not found in cart " + command.cartId());
        }

        log.debug("applyRemoval - End removed=%s newSize=%s", removed, updated.size());
        return updated;
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.clientIp(), context.userAgent());
    }
}
