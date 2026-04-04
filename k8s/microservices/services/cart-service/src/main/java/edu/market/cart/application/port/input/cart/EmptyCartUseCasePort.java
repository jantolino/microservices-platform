package edu.market.cart.application.port.input.cart;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para vaciar completamente un carrito.
 */
public interface EmptyCartUseCasePort {

    /**
     * Elimina todos los ítems del carrito.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado (sin ítems)
     */
    CartResponse empty(CartUserCommand command, ClientContextCommand context);
}
