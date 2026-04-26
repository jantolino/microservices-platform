package edu.market.cart.application.port.input.session;

import edu.market.cart.application.dto.request.ClientContextCommand;
import edu.market.cart.application.dto.response.CartResponse;
import java.util.UUID;

/**
 * Puerto de caso de uso para migrar un carrito de invitado a un usuario autenticado.
 */
public interface MigrateGuestCartUseCasePort {

    /**
     * Migra/mergea el carrito de invitado al carrito del usuario autenticado.
     *
     * @param guestSessionId identificador de sesión de invitado
     * @param userId         identificador del usuario autenticado
     * @param context        contexto del cliente para trazabilidad
     * @return carrito resultante tras la migración
     */
    CartResponse migrate(String guestSessionId, UUID userId, ClientContextCommand context);
}
