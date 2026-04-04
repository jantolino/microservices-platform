package edu.market.cart.application.usecase.cart;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.CreateCartCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.cart.CreateCartUseCasePort;
import edu.market.cart.domain.model.Cart;
import edu.market.cart.domain.model.CartTotals;
import edu.market.cart.domain.model.CartItem;
import edu.market.cart.domain.port.output.monitoring.LoggingPort;
import edu.market.cart.domain.port.output.persistence.CartRepositoryPort;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Caso de uso para crear un carrito.
 *
 * Responsabilidades:
 * - Inicializar un carrito vacío para usuario autenticado o sesión invitada.
 * - Persistir el agregado Cart.
 * - Registrar trazabilidad y devolver DTO de aplicación.
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper).
 * - Manejo de errores con ApplicationException y logging detallado.
 */
public class CreateCartUseCase implements CreateCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public CreateCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Crea un carrito para el usuario/sesión indicada.
     *
     * @param command datos de creación
     * @param context contexto del cliente para trazabilidad
     * @return carrito creado
     */
    @Override
    public CartResponse create(CreateCartCommand command, ClientContextCommand context) {
        log.info("create - Init userId=%s sessionId=%s context=%s", command.userId(), command.sessionId(), safeContext(context));
        try {
            log.debug("create - Build new cart");
            Cart cart = new Cart.Builder()
                    .withId(UUID.randomUUID())
                    .withUserId(command.userId())
                    .withSessionId(command.sessionId())
                    .withItems(Collections.emptyList())
                    .withTotals(CartTotals.zero())
                    .withCoupons(Collections.emptyList())
                    .withCreatedAt(OffsetDateTime.now())
                    .withUpdatedAt(OffsetDateTime.now())
                    .build();

            log.debug("create - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("create - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("create - End cartId=%s", saved.getId());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error creating cart for user " + command.userId() + ": " + ex.getMessage();
            log.error("create - Exception occurred userId=%s sessionId=%s", command.userId(), command.sessionId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.ipAddress(), context.userAgent());
    }
}
