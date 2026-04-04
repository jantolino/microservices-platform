package edu.market.cart.application.port.input.shipping;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.SelectShippingMethodCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para seleccionar el método de envío preferido.
 */
public interface SelectShippingMethodUseCasePort {

    /**
     * Selecciona el método de envío para el carrito.
     *
     * @param command datos del método de envío
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con el método de envío seleccionado
     */
    CartResponse select(SelectShippingMethodCommand command, ClientContextCommand context);
}
