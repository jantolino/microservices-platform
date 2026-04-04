package edu.market.cart.application.usecase.session;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.session.SaveCartUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para guardar/persistir un carrito de usuario autenticado.
 *
 * Responsabilidades:
 * - Recuperar el carrito y validarlo para el usuario.
 * - Persistir cambios del carrito.
 * - Devolver DTO de aplicación.
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class SaveCartUseCase implements SaveCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public SaveCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Persiste el carrito asociado a un usuario.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito persistido
     */
    @Override
    public CartResponse save(CartUserCommand command, ClientContextCommand context) {
        log.info("save - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));
        try {
            log.debug("save - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            // TODO: aplicar cambios pendientes antes de guardar (si aplica)

            log.debug("save - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("save - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("save - End cartId=%s", saved.getId());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error saving cart " + command.cartId() + ": " + ex.getMessage();
            log.error("save - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
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

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
