package edu.market.cart.application.port.input.pricing;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para recalcular totales del carrito.
 */
public interface CalculateTotalsUseCasePort {

    /**
     * Calcula subtotal, impuestos, envío estimado y total final.
     *
     * @param command datos del carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con totales recalculados
     */
    CartResponse calculate(CartUserCommand command, ClientContextCommand context);
}
