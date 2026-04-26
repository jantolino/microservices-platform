package edu.market.cart.application.port.input.session;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para guardar/persistir un carrito de usuario autenticado.
 */
public interface SaveCartUseCasePort {

    /**
     * Persiste el carrito asociado a un usuario.
     *
     * @param command datos de carrito/usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito persistido
     */
    CartResponse save(CartUserCommand command, ClientContextCommand context);
}
