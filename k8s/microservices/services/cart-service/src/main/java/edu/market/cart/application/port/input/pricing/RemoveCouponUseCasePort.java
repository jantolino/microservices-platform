package edu.market.cart.application.port.input.pricing;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.CouponCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para remover un cupón del carrito.
 */
public interface RemoveCouponUseCasePort {

    /**
     * Remueve un cupón del carrito.
     *
     * @param command datos del cupón a remover
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    CartResponse remove(CouponCommand command, ClientContextCommand context);
}
