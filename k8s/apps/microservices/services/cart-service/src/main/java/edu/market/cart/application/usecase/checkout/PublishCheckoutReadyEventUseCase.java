package edu.market.cart.application.usecase.checkout;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.port.input.checkout.PublishCheckoutReadyEventUseCasePort;
import edu.market.cart.domain.exception.CartException;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;
import edu.market.cart.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.cart.domain.model.TransactionalOutbox;
import edu.market.cart.domain.enums.EventStatusType;
import edu.market.cart.domain.enums.CartEventType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para publicar el evento de carrito listo para checkout.
 *
 * Responsabilidades:
 * - Validar la existencia del carrito y su elegibilidad.
 * - Registrar el evento en outbox para ser procesado y publicado.
 * - Registrar trazabilidad (logging) y manejar errores con ApplicationException.
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, TransactionalOutboxRepositoryPort, LoggingPort).
 * - Transactional Outbox (persistir el evento para publicación asíncrona).
 */
public class PublishCheckoutReadyEventUseCase implements PublishCheckoutReadyEventUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final TransactionalOutboxRepositoryPort outboxRepository;
    private final LoggingPort log;

    public PublishCheckoutReadyEventUseCase(CartRepositoryPort cartRepository,
                                            TransactionalOutboxRepositoryPort outboxRepository,
                                            LoggingPort log) {
        this.cartRepository = cartRepository;
        this.outboxRepository = outboxRepository;
        this.log = log;
    }

    /**
     * Publica el evento que indica que el carrito está listo para checkout.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     */
    @Override
    public void publish(CartUserCommand command, ClientContextCommand context) {
        log.info("publish - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));

        try {
            log.debug("publish - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("publish - Call enqueueCheckoutReadyEvent");
            enqueueCheckoutReadyEvent(cart);

            log.info("publish - End cartId=%s", cart.getId());
        } catch (Exception ex) {
            String errorMessage = "Error publishing checkout ready for cart " + command.cartId() + ": " + ex.getMessage();
            log.error("publish - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private void enqueueCheckoutReadyEvent(Cart cart) {
        log.debug("enqueueCheckoutReadyEvent - Init cartId=%s", cart.getId());
        TransactionalOutbox event = TransactionalOutbox.builder()
                .withId(UUID.randomUUID())
                .withStatusType(EventStatusType.PENDING)
                .withAggregateId(cart.getId().toString())
                .withPayload("{}") // TODO: serializar payload real del evento checkout-ready
                .withCreatedAt(LocalDateTime.now())
                .withProcessed(false)
                .withProcessedAt(null)
                .withRetryCount(0)
                .withMessage("")
                .withLastRetryAt(null)
                .withEventType(CartEventType.CHECKOUT_READY)
                .build();
        log.debug("enqueueCheckoutReadyEvent - Call outboxRepository.save");
        outboxRepository.save(event);
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
