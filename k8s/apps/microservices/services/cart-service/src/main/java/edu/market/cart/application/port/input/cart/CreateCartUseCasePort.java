package edu.market.cart.application.port.input.cart;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.request.CreateCartCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para crear un carrito.
 *
 * Este caso de uso inicializa un carrito vacío para un usuario o sesión invitado.
 */
public interface CreateCartUseCasePort {

    /**
     * Crea un carrito para el usuario/sesión indicada.
     *
     * @param command datos de creación
     * @param context contexto del cliente para trazabilidad
     * @return agregado Cart creado
     */
    CartResponse create(CreateCartCommand command, ClientContextCommand context);
}
