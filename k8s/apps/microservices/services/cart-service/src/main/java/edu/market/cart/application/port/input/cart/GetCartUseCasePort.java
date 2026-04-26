package edu.market.cart.application.port.input.cart;

import edu.market.cart.application.dto.request.CartUserCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para consultar un carrito existente.
 */
public interface GetCartUseCasePort {

    /**
     * Obtiene un carrito por su identificador y opcionalmente valida pertenencia del usuario.
     *
     * @param query   datos de consulta
     * @param context contexto del cliente para trazabilidad
     * @return carrito como respuesta de aplicación
     */
    CartResponse getById(CartUserCommand command, ClientContextCommand context);
}
