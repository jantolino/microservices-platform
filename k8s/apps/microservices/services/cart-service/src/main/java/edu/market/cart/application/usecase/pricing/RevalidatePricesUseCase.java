package edu.market.cart.application.usecase.pricing;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.pricing.RevalidatePricesUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.model.CartItem;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para revalidar precios y promociones del carrito.
 *
 * Responsabilidades:
 * - Consultar precios/promos vigentes y ajustar líneas (placeholder).
 * - Persistir el carrito con los precios revalidados.
 * - Devolver DTO de aplicación.
 */
public class RevalidatePricesUseCase implements RevalidatePricesUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public RevalidatePricesUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Revalida precios, promociones y descuentos vigentes sobre el carrito.
     *
     * @param command datos del carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con precios revalidados
     */
    @Override
    public CartResponse revalidate(CartUserCommand command, ClientContextCommand context) {
        log.info("revalidate - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));
        try {
            log.debug("revalidate - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("revalidate - Call repriceItems (TODO)");
            List<CartItem> repriced = repriceItems(cart);
            cart.replaceItems(repriced);

            log.debug("revalidate - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("revalidate - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("revalidate - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error revalidating prices for cart " + command.cartId() + ": " + ex.getMessage();
            log.error("revalidate - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
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

    private List<CartItem> repriceItems(Cart cart) {
        log.debug("repriceItems - Init items=%s", cart.getItems().size());
        // TODO: integrar con servicio de precios/promociones. Placeholder: recalcula lineTotal con mismos unitPrice.
        List<CartItem> updated = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            BigDecimal newLineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            CartItem updatedItem = CartItem.builder()
                    .withId(item.getId())
                    .withProductId(item.getProductId())
                    .withVariantId(item.getVariantId())
                    .withQuantity(item.getQuantity())
                    .withUnitPrice(item.getUnitPrice())
                    .withLineTotal(newLineTotal)
                    .build();
            updated.add(updatedItem);
        }
        log.debug("repriceItems - End items=%s", updated.size());
        return updated;
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
