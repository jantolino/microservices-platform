package edu.market.cart.application.port.input.cart;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.RemoveItemCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para eliminar un ítem del carrito.
 */
public interface RemoveItemFromCartUseCasePort {

    /**
     * Elimina el ítem indicado del carrito.
     *
     * @param command datos del ítem a eliminar
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    CartResponse removeItem(RemoveItemCommand command, ClientContextCommand context);
}
