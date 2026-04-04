package edu.market.cart.application.usecase.checkout;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.checkout.PrepareCheckoutUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para preparar el checkout.
 *
 * Responsabilidades:
 * - Validar stock, precios, límites y precondiciones antes del pago.
 * - Marcar el carrito en estado listo para checkout (placeholder).
 * - Persistir y devolver DTO de aplicación.
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class PrepareCheckoutUseCase implements PrepareCheckoutUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public PrepareCheckoutUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Ejecuta las validaciones previas al checkout.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito validado/preparado
     */
    @Override
    public CartResponse prepare(CartUserCommand command, ClientContextCommand context) {
        log.info("prepare - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));

        try {
            log.debug("prepare - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("prepare - Call validatePreconditions");
            validatePreconditions(cart);

            log.debug("prepare - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("prepare - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("prepare - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error preparing checkout for cart " + command.cartId() + ": " + ex.getMessage();
            log.error("prepare - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private void validatePreconditions(Cart cart) {
        log.debug("validatePreconditions - Init items=%s", cart.getItems().size());
        // TODO: validar stock, precios, estado, límites, envío, etc.
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
