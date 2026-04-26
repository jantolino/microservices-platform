package edu.market.cart.application.port.input.pricing;

import edu.market.cart.application.dto.request.CouponCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para aplicar o remover un cupón en el carrito.
 */
public interface ApplyCouponUseCasePort {

    /**
     * Aplica un cupón al carrito.
     *
     * @param command datos del cupón a aplicar
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    CartResponse apply(CouponCommand command, ClientContextCommand context);
}
