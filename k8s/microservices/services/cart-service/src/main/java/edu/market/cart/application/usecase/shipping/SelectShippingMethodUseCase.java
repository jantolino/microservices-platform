package edu.market.cart.application.usecase.shipping;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.SelectShippingMethodCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.shipping.SelectShippingMethodUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.util.Optional;

/**
 * Caso de uso para seleccionar el método de envío preferido.
 *
 * Responsabilidades:
 * - Validar y asignar el método de envío al carrito (placeholder).
 * - Persistir y devolver DTO de aplicación.
 */
public class SelectShippingMethodUseCase implements SelectShippingMethodUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public SelectShippingMethodUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Selecciona el método de envío para el carrito.
     *
     * @param command datos del método de envío
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con el método de envío seleccionado
     */
    @Override
    public CartResponse select(SelectShippingMethodCommand command, ClientContextCommand context) {
        log.info("select - Init cartId=%s userId=%s methodId=%s context=%s",
                command.cartId(), command.userId(), command.methodId(), safeContext(context));
        try {
            log.debug("select - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("select - Call applyShippingMethod (TODO)");
            applyShippingMethod(cart, command.methodId());

            log.debug("select - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("select - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("select - End cartId=%s methodId=%s", saved.getId(), command.methodId());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error selecting shipping method " + command.methodId() + " for cart " + command.cartId() + ": " + ex.getMessage();
            log.error("select - Exception occurred cartId=%s userId=%s methodId=%s", command.cartId(), command.userId(), command.methodId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(java.util.UUID cartId, java.util.UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        Cart result = cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId));
        if (result.getUserId() != null && !result.getUserId().equals(userId)) {
            throw new CartException("Cart " + cartId + " does not belong to user " + userId);
        }
        return result;
    }

    private void applyShippingMethod(Cart cart, String methodId) {
        log.debug("applyShippingMethod - Init cartId=%s methodId=%s", cart.getId(), methodId);
        // TODO: validar método de envío contra catálogo/logística, calcular costo y asignar a totales.
        // Placeholder: no persiste método en dominio aún; dejar hook para Totals cuando exista el campo.
        log.debug("applyShippingMethod - End cartId=%s methodId=%s", cart.getId(), methodId);
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
