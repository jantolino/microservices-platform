package edu.market.cart.application.usecase.cart;

import edu.market.cart.application.dto.request.AddItemCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.port.input.cart.AddItemToCartUseCasePort;
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
 * Caso de uso para agregar un ítem al carrito.
 *
 * Responsabilidades:
 * - Validar y orquestar la operación de agregado de ítems en el carrito.
 * - Persistir el agregado Cart tras la modificación.
 * - Registrar trazabilidad y generar la respuesta de aplicación (CartResponse).
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper) para no exponer dominio.
 * - Manejo de errores centralizado con logging detallado.
 */
public class AddItemToCartUseCase implements AddItemToCartUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public AddItemToCartUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Agrega un ítem al carrito indicado.
     *
     * @param command datos del ítem a agregar
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    @Override
    public CartResponse addItem(AddItemCommand command, ClientContextCommand context) {
        
        log.info("addItem - Init cartId=%s userId=%s context=%s", command.cartId(), command.userId(), safeContext(context));

        try {
            log.debug("addItem - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("addItem - Building cart item productId=%s variantId=%s qty=%s", command.productId(), command.variantId(), command.quantity());
            CartItem item = CartItem.builder()
                    .withId(UUID.randomUUID())
                    .withProductId(command.productId())
                    .withVariantId(command.variantId())
                    .withQuantity(command.quantity())
                    .withUnitPrice(command.unitPrice())
                    .withLineTotal(calculateLineTotal(command))
                    .build();

            log.debug("addItem - Merging item into cart itemsBefore=%s", cart.getItems().size());
            cart.replaceItems(mergeItems(cart, item, command));

            log.debug("addItem - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("addItem - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);
            
            log.info("addItem - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error adding item to cart " + command.cartId() + ": " + ex.getMessage();
            log.error("addItem - Exception occurred cartId=%s userId=%s", command.cartId(), command.userId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("fetchCart - Init cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private BigDecimal calculateLineTotal(AddItemCommand command) {
        log.debug("calculateLineTotal - Init qty=%s unitPrice=%s", command.quantity(), command.unitPrice());
        return command.unitPrice() != null
                ? command.unitPrice().multiply(BigDecimal.valueOf(command.quantity()))
                : BigDecimal.ZERO;
    }

    private List<CartItem> mergeItems(Cart cart, CartItem newItem, AddItemCommand command) {
        log.debug("mergeItems - Init existing=%s", cart.getItems().size());
        List<CartItem> updated = new ArrayList<>();

        boolean merged = false;
        for (CartItem current : cart.getItems()) {
            if (isSameItem(current, command)) {
                int newQty = current.getQuantity() + command.quantity();
                BigDecimal unitPrice = command.unitPrice() != null ? command.unitPrice() : current.getUnitPrice();
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(newQty));
                CartItem mergedItem = CartItem.builder()
                        .withId(current.getId())
                        .withProductId(current.getProductId())
                        .withVariantId(current.getVariantId())
                        .withQuantity(newQty)
                        .withUnitPrice(unitPrice)
                        .withLineTotal(lineTotal)
                        .build();
                updated.add(mergedItem);
                merged = true;
            } else {
                updated.add(current);
            }
        }

        if (!merged) {
            updated.add(newItem);
        }

        log.debug("mergeItems - End merged=%s newSize=%s", merged, updated.size());
        return updated;
    }

    private boolean isSameItem(CartItem current, AddItemCommand command) {
        log.debug("isSameItem - Init productId=%s variantId=%s", current.getProductId(), current.getVariantId());
        return current.getProductId().equals(command.productId()) &&
                ((current.getVariantId() == null && command.variantId() == null) ||
                 (current.getVariantId() != null && current.getVariantId().equals(command.variantId())));
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.clientIp(), context.userAgent());
    }
}
