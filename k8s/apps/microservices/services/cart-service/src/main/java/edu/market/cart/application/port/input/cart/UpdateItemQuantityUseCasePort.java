package edu.market.cart.application.port.input.cart;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.UpdateItemQuantityCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para actualizar la cantidad de un ítem del carrito.
 *
 * Este caso de uso ajusta la cantidad de una línea existente en el carrito.
 */
public interface UpdateItemQuantityUseCasePort {

    /**
     * Actualiza la cantidad de un ítem existente.
     *
     * @param command datos de actualización
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    CartResponse updateQuantity(UpdateItemQuantityCommand command, ClientContextCommand context);
}
