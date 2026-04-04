package edu.market.cart.application.port.input.inventory;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;


/**
 * Puerto de caso de uso para reservar stock temporalmente durante una ventana corta.
 */
public interface ReserveStockTemporarilyUseCasePort {

    /**
     * Solicita la reserva temporal de stock para los ítems del carrito.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con el estado de reserva reflejado (si aplica)
     */
    CartResponse reserve(CartUserCommand command, ClientContextCommand context);
}
