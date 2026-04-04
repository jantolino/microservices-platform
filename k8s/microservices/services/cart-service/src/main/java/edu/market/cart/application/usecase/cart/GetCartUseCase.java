package edu.market.cart.application.usecase.cart;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.cart.GetCartUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para consultar un carrito existente.
 *
 * Responsabilidades:
 * - Recuperar el carrito por id y validar pertenencia.
 * - Mapear a DTO de aplicación (CartResponse).
 * - Registrar trazabilidad y manejar errores con ApplicationException.
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 */
public class GetCartUseCase implements GetCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public GetCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Obtiene un carrito por su identificador y valida pertenencia.
     *
     * @param command datos de consulta
     * @param context contexto del cliente para trazabilidad
     * @return carrito como respuesta de aplicación
     */
    @Override
    public CartResponse getById(CartUserCommand command, ClientContextCommand context) {
        log.info("getById - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));
        try {
            log.debug("getById - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("getById - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(cart);

            log.info("getById - End cartId=%s items=%s", cart.getId(), cart.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error getting cart " + command.cartId() + ": " + ex.getMessage();
            log.error("getById - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        Cart result = cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId));
        // Validación de pertenencia (placeholder)
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
