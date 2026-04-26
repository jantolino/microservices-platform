package edu.market.cart.application.port.input.cart;

import edu.market.cart.application.dto.request.AddItemCommand;
import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

/**
 * Puerto de caso de uso para agregar un ítem al carrito.
 *
 * Este caso de uso agrega una línea con producto, variante, cantidad y precio unitario.
 */
public interface AddItemToCartUseCasePort {

    /**
     * Agrega un ítem al carrito indicado.
     *
     * @param command datos del ítem a agregar
     * @param context contexto del cliente para trazabilidad
     * @return carrito actualizado
     */
    CartResponse addItem(AddItemCommand command, ClientContextCommand context);
}
