package edu.market.cart.application.usecase.cart;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.UpdateItemQuantityCommand;
import edu.market.cart.application.dto.response.CartResponse;
import edu.market.cart.application.exception.ApplicationException;
import edu.market.cart.application.mapper.CartMapper;
import edu.market.cart.application.port.input.cart.UpdateItemQuantityUseCasePort;
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
 * Caso de uso para actualizar la cantidad de un ítem del carrito.
 *
 * Responsabilidades:
 * - Validar y orquestar la actualización de cantidad de una línea existente.
 * - Persistir el agregado Cart tras la modificación.
 * - Registrar trazabilidad y devolver DTO de aplicación (CartResponse).
 *
 * Patrones aplicados:
 * - Arquitectura Hexagonal (puerto de entrada + puertos de salida).
 * - Inversión de dependencias (CartRepositoryPort, LoggingPort).
 * - Mapper de salida (CartMapper) para no exponer dominio.
 * - Manejo de errores centralizado con ApplicationException y logging detallado.
 */
public class UpdateItemQuantityUseCase implements UpdateItemQuantityUseCasePort {

    private final CartRepositoryPort cartRepository;
    private final LoggingPort log;

    public UpdateItemQuantityUseCase(CartRepositoryPort cartRepository, LoggingPort log) {
        this.cartRepository = cartRepository;
        this.log = log;
    }

    /**
     * Actualiza la cantidad de un ítem existente.
     *
     * @param command datos de actualización
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    @Override
    public CartResponse updateQuantity(UpdateItemQuantityCommand command, ClientContextCommand context) {
        log.info("updateQuantity - Init cartId=%s userId=%s itemId=%s context=%s",
                command.cartId(), command.userId(), command.itemId(), safeContext(context));

        try {
            log.debug("updateQuantity - Call fetchCart");
            Cart cart = fetchCart(command.cartId(), command.userId());

            log.debug("updateQuantity - Call applyQuantityChange");
            cart.replaceItems(applyQuantityChange(cart, command));

            log.debug("updateQuantity - Call cartRepository.save");
            Cart saved = cartRepository.save(cart);

            log.debug("updateQuantity - Call CartMapper.toResponse");
            CartResponse response = CartMapper.toResponse(saved);

            log.info("updateQuantity - End cartId=%s items=%s", saved.getId(), saved.getItems().size());
            return response;
        } catch (Exception ex) {
            String errorMessage = "Error updating item quantity in cart " + command.cartId() + ": " + ex.getMessage();
            log.error("updateQuantity - Exception occurred cartId=%s userId=%s itemId=%s", command.cartId(), command.userId(), command.itemId(), ex);
            throw new ApplicationException(errorMessage, ex);
        }
    }

    private Cart fetchCart(UUID cartId, UUID userId) {
        log.debug("updateQuantity - fetchCart cartId=%s userId=%s", cartId, userId);
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new CartException("Cart not found for id " + cartId + " and user " + userId));
    }

    private List<CartItem> applyQuantityChange(Cart cart, UpdateItemQuantityCommand command) {
        log.debug("updateQuantity - applyQuantityChange items=%s targetItem=%s", cart.getItems().size(), command.itemId());
        List<CartItem> updated = new ArrayList<>();
        boolean found = false;

        for (CartItem current : cart.getItems()) {
            if (current.getId().equals(command.itemId())) {
                int newQty = command.quantity();
                BigDecimal unitPrice = current.getUnitPrice();
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(newQty));
                CartItem updatedItem = CartItem.builder()
                        .withId(current.getId())
                        .withProductId(current.getProductId())
                        .withVariantId(current.getVariantId())
                        .withQuantity(newQty)
                        .withUnitPrice(unitPrice)
                        .withLineTotal(lineTotal)
                        .build();
                updated.add(updatedItem);
                found = true;
            } else {
                updated.add(current);
            }
        }

        if (!found) {
            throw new CartException("Item " + command.itemId() + " not found in cart " + command.cartId());
        }

        log.debug("updateQuantity - applyQuantityChange done found=%s newSize=%s", found, updated.size());
        return updated;
    }

    private String safeContext(ClientContextCommand context) {
        if (context == null) {
            return "null";
        }
        return String.format("ip=%s userAgent=%s", context.clientIp(), context.userAgent());
    }
}
