package edu.market.cart.application.port.input.pricing;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para revalidar precios y promociones del carrito.
 */
public interface RevalidatePricesUseCasePort {

    /**
     * Revalida precios, promociones y descuentos vigentes sobre el carrito.
     *
     * @param command datos del carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado con precios revalidados
     */
    CartResponse revalidate(CartUserCommand command, ClientContextCommand context);
}
