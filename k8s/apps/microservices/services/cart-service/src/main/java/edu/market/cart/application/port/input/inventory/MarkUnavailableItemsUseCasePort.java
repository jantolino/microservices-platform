package edu.market.cart.application.port.input.inventory;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para marcar ítems no disponibles (stock/precio).
 */
public interface MarkUnavailableItemsUseCasePort {

    /**
     * Marca como no disponibles los ítems afectados (sin stock o con cambio de precio).
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    CartResponse markUnavailable(CartUserCommand command, ClientContextCommand context);
}
