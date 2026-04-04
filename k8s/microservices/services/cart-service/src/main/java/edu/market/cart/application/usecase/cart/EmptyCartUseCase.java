package edu.market.cart.application.usecase.cart;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.cart.EmptyCartUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para vaciar completamente un carrito.
 *
 * Responsabilidades:
 * - Orquestar la eliminación de todos los ítems del carrito.
 * - Persistir el agregado Cart tras el cambio.
 * - Registrar trazabilidad y devolver DTO de aplicación (CartResponse).
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class EmptyCartUseCase implements EmptyCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public EmptyCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Elimina todos los ítems del carrito.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado (sin ítems)
     */
    @Override
    public CartResponse empty(CartUserCommand command, ClientContextCommand context) {
        log.info("empty - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));

        try {
            log.debug("empty - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("empty - Clearing items");
            cart.replaceItems(Collections.emptyList());

            log.debug("empty - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("empty - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("empty - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error emptying cart " + command.cartId() + ": " + ex.getMessage();
            log.error("empty - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.clientIp(), context.userAgent());
    }
}
