package edu.market.cart.application.port.input.checkout;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para preparar el checkout.
 * Valida stock, precios, límites y cualquier precondición antes de iniciar el pago/pedido.
 */
public interface PrepareCheckoutUseCasePort {

    /**
     * Ejecuta las validaciones previas al checkout.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito validado/preparado
     */
    CartResponse prepare(CartUserCommand command, ClientContextCommand context);
}
