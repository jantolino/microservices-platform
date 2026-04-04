package edu.market.notification.application.port.input.user;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.DisableNotificationsCommand;

/**
 * Puerto de caso de uso para deshabilitar notificaciones para un usuario.
 * 
 * Este caso de uso permite a los usuarios optar por no recibir notificaciones,
 * ya sea globalmente o para tipos específicos de notificaciones.
 */
public interface DisableNotificationsUseCasePort {
    
    /**
     * Deshabilita notificaciones para un usuario.
     * 
     * @param command El comando con los datos para deshabilitar notificaciones
     * @return true si las notificaciones se deshabilitaron correctamente, false en caso contrario
     */
    boolean disable(DisableNotificationsCommand command, ClientContextCommand clientContext);
}
