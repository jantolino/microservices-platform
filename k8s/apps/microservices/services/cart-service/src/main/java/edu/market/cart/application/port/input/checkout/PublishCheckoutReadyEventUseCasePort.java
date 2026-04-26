package edu.market.cart.application.port.input.checkout;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;

/**
 * Puerto de caso de uso para publicar el evento de checkout listo hacia order-service (u otros).
 */
public interface PublishCheckoutReadyEventUseCasePort {

    /**
     * Publica el evento que indica que el carrito está listo para checkout.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     */
    void publish(CartUserCommand command, ClientContextCommand context);
}
