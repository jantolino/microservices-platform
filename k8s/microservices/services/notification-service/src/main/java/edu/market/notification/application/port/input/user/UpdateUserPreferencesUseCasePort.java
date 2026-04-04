package edu.market.notification.application.port.input.user;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.UpdateUserPreferencesCommand;
import edu.market.notification.application.dto.response.UserPreferenceResponse;

/**
 * Puerto de caso de uso para actualizar preferencias de notificación por usuario.
 * 
 * Este caso de uso permite a los usuarios actualizar sus preferencias sobre qué tipos
 * de notificaciones desean recibir y a través de qué canales.
 */
public interface UpdateUserPreferencesUseCasePort {
    
    /**
     * Actualiza las preferencias de notificación de un usuario.
     * 
     * @param command Value Object con las preferencias actualizadas del usuario
     * @return Value Object con las preferencias del usuario actualizadas
     */
    UserPreferenceResponse update(UpdateUserPreferencesCommand command, ClientContextCommand clientContext);
}
