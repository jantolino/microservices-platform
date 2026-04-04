package edu.market.cart.application.port.input.session;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;

import java.util.UUID;

/**
 * Puerto de caso de uso para recuperar/restaurar un carrito guardado.
 */
public interface RecoverCartUseCasePort {

    /**
     * Recupera el carrito guardado de un usuario.
     *
     * @param userId  identificador del usuario
     * @param context contexto del cliente para trazabilidad
     * @return carrito restaurado
     */
    CartResponse recover(UUID userId, ClientContextCommand context);
}
