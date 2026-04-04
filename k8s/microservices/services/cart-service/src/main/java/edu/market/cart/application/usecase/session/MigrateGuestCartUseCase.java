package edu.market.cart.application.usecase.session;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.session.MigrateGuestCartUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.model.CartItem;
import edu.market.cart.domain.model.CartTotals;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para migrar un carrito de invitado a un usuario autenticado.
 *
 * Responsabilidades:
 * - Obtener carrito de invitado y carrito activo del usuario (si existe).
 * - Mergear ítems/cupones y persistir carrito resultante.
 * - Eliminar carrito invitado si procede.
 * - Registrar trazabilidad y manejar errores con ApplicationException.
 */
public class MigrateGuestCartUseCase implements MigrateGuestCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public MigrateGuestCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Migra/mergea el carrito de invitado al carrito del usuario autenticado.
     *
     * @param guestSessionId identificador de sesión de invitado
     * @param userId         identificador del usuario autenticado
     * @param context        contexto del cliente para trazabilidad
     * @return carrito resultante tras la migración
     */
    @Override
    public CartResponse migrate(String guestSessionId, UUID userId, ClientContextCommand context) {
        log.info("migrate - Init guestSessionId=%s userId=%s context=%s", guestSessionId, userId, safeContext(context));
        try {
            log.debug("migrate - Call fetchGuestCart");
            Cart guestCart = fetchGuestCart(guestSessionId);

            log.debug("migrate - Call fetchUserCartIfAny");
            Optional<Cart> userCartOpt = cartRepository.findActiveByUserId(userId);

            Cart merged = userCartOpt.map(userCart -> mergeCarts(userCart, guestCart))
                    .orElseGet(() -> buildCartForUserFromGuest(userId, guestSessionId, guestCart));

            log.debug("migrate - Call cartRepository.save");
            Cart saved = cartRepository.save(merged);

            log.debug("migrate - Call cleanupGuestCart");
            cleanupGuestCart(guestSessionId);

            log.debug("migrate - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("migrate - End cartId=%s", saved.getId());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error migrating guest cart " + guestSessionId + " for user " + userId + ": " + ex.getMessage();
            log.error("migrate - Exception occurred guestSessionId=%s userId=%s", guestSessionId, userId, ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchGuestCart(String guestSessionId) {
        log.debug("fetchGuestCart - Init sessionId=%s", guestSessionId);
        return cartRepository.findActiveBySessionId(guestSessionId)
                .orElseThrow(() -> new CartException("Guest cart not found for session " + guestSessionId));
    }

    private Cart mergeCarts(Cart userCart, Cart guestCart) {
        log.debug("mergeCarts - Init userCartId=%s guestCartId=%s", userCart.getId(), guestCart.getId());
        List<CartItem> mergedItems = new ArrayList<>(userCart.getItems());
        mergedItems.addAll(guestCart.getItems());
        userCart.replaceItems(mergedItems);

        // TODO: merge cupones o recalcular precios según reglas de negocio
        userCart.replaceCoupons(userCart.getCoupons());

        return userCart;
    }

    private Cart buildCartForUserFromGuest(UUID userId, String sessionId, Cart guestCart) {
        log.debug("buildCartForUserFromGuest - Init userId=%s sessionId=%s", userId, sessionId);
        return new Cart.Builder()
                .withId(UUID.randomUUID())
                .withUserId(userId)
                .withSessionId(sessionId)
                .withItems(guestCart.getItems())
                .withCoupons(guestCart.getCoupons())
                .withTotals(guestCart.getTotals() != null ? guestCart.getTotals() : CartTotals.zero())
                .withStatus(guestCart.getStatus())
                .withCreatedAt(OffsetDateTime.now())
                .withUpdatedAt(OffsetDateTime.now())
                .build();
    }

    private void cleanupGuestCart(String guestSessionId) {
        log.debug("cleanupGuestCart - Init sessionId=%s", guestSessionId);
        cartRepository.deleteBySessionId(guestSessionId);
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
