package edu.market.cart.application.usecase.session;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.session.RecoverCartUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para recuperar/restaurar un carrito guardado.
 *
 * Responsabilidades:
 * - Buscar carrito activo por usuario.
 * - Mapear a DTO de aplicación.
 * - Registrar trazabilidad y manejar errores con ApplicationException.
 */
public class RecoverCartUseCase implements RecoverCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public RecoverCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Recupera el carrito guardado de un usuario.
     *
     * @param userId  identificador del usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito restaurado
     */
    @Override
    public CartResponse recover(UUID userId, ClientContextCommand context) {
        log.info("recover - Init userId=%s context=%s", userId, safeContext(context));
        try {
            log.debug("recover - Call fetchActiveCartByUser");
            Cart cart = fetchActiveCartByUser(userId);

            log.debug("recover - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(cart);

            log.info("recover - End cartId=%s", cart.getId());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error recovering cart for user " + userId + ": " + ex.getMessage();
            log.error("recover - Exception occurred userId=%s", userId, ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchActiveCartByUser(UUID userId) {
        log.debug("fetchActiveCartByUser - Init userId=%s", userId);
        Optional<Cart> cart = cartRepository.findActiveByUserId(userId);
        return cart.orElseThrow(() -> new CartException("Active cart not found for user " + userId));
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
